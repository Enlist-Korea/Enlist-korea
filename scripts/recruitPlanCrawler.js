import fs from "fs";
import * as cheerio from "cheerio";
// eslint-disable-next-line
import * as CARWL from "./crawlerConstants.js";

/**
 * 텍스트 정제 함수
 */
function cleanText(text) {
  if (!text) return "";
  return text
    .replace(/[\n\t]/g, " ")
    .replace(/\s+/g, " ")
    .trim();
}

/**
 * 날짜 형식이 텍스트에 포함되어 있는지 확인하는 헬퍼 함수
 * 예: '25. 11. 27' 또는 '2025-11-27' 같은 패턴 감지
 */
function isDateString(text) {
  // '25. 11. 27 처럼 시작하거나 숫자가 많은 경우 감지
  return /^['‘]?\d{2}\.\s?\d{1,2}\./.test(text) || /^\d{4}-\d{2}/.test(text);
}

/**
 * 육군 모집계획 테이블을 파싱하여 JSON으로 변환하는 함수
 */
function parseRecruitTable(htmlText) {
  const $ = cheerio.load(htmlText);
  // 테이블의 tbody 내의 모든 행 선택
  const $rows = $("div.table_scroll table.table_col tbody tr");

  const TOTAL_COLS = 8;
  const TOTAL_ROWS = $rows.length;

  // 2차원 매트릭스 생성
  const grid = Array.from({ length: TOTAL_ROWS }, () =>
    Array(TOTAL_COLS).fill(null),
  );

  // HTML 순회하며 매트릭스 채우기
  $rows.each((rowIndex, tr) => {
    let colIndex = 0;

    $(tr)
      .find("td")
      .each((_, td) => {
        // 현재 행에서 비어있는 칸 찾기 (rowspan으로 채워진 곳 건너뛰기)
        while (colIndex < TOTAL_COLS && grid[rowIndex][colIndex] !== null) {
          colIndex++;
        }

        const $td = $(td);
        let text = cleanText($td.text());
        const rowspan = parseInt($td.attr("rowspan") || "1", 10);
        const colspan = parseInt($td.attr("colspan") || "1", 10);

        // 매트릭스 채우기
        for (let r = 0; r < rowspan; r++) {
          for (let c = 0; c < colspan; c++) {
            const targetRow = rowIndex + r;
            const targetCol = colIndex + c;

            if (targetRow < TOTAL_ROWS && targetCol < TOTAL_COLS) {
              grid[targetRow][targetCol] = text;
            }
          }
        }
        colIndex += colspan;
      });
  });

  // 매트릭스를 JSON 객체로 변환 및 필터링
  const results = grid
    .map((row) => {
      const division = row[0] || "";
      const category = row[1] || "";
      let name = row[2] || "";

      // colspan 처리 보정 (카테고리와 이름이 같은 경우)
      if (category === name) {
        name = category;
      }

      return {
        division: division, // 군별
        category: category, // 모집분야(대분류)
        name: name, // 모집분야(상세)
        period: row[3], // 접수기간
        date_step1: row[4], // 1차발표
        date_final: row[5], // 최종발표
        enlist_month: row[6], // 입영월
        count: row[7], // 모집인원
      };
    })
    .filter((item) => {
      // [중요] 유효성 검사: 쓰레기 데이터 제거

      // 1. 이름이나 카테고리가 비어있으면 제외
      if (!item.name || !item.category) return false;

      // 2. 이름이나 카테고리가 '날짜 형식'이면 제외 (데이터 밀림 방지)
      // 예: 이름 칸에 '25. 11. 27...'이 들어온 경우
      if (isDateString(item.name) || isDateString(item.category)) {
        return false;
      }

      // 3. 모집인원이 없거나 이상한 경우 제외
      if (!item.count) return false;

      return true;
    });

  return results;
}

/**
 * 이달의 모집계획 크롤링 메인 함수
 */
export async function crawlRecruitPlan(page) {
  console.log("📅 이달의 모집계획(육군) 페이지 이동 중...");

  await page.goto("https://www.mma.go.kr/contents.do?mc=mma0000743", {
    waitUntil: "domcontentloaded",
    timeout: 60000,
  });

  console.log("✅ 모집계획 페이지 로딩 완료, 테이블 파싱 시작...");
  const html = await page.content();

  try {
    const data = parseRecruitTable(html);

    const savePath =
      CARWL.PATH.MMA_RECRUIT_OUT || "./src/front/data/recruitPlan.json";

    fs.writeFileSync(savePath, JSON.stringify(data, null, 2), "utf8");
    console.log(
      `💾 ${data.length}개 모집계획 데이터 저장 완료 (유효성 검사됨) → ${savePath}`,
    );
  } catch (error) {
    console.error("❌ 모집계획 파싱 실패:", error);
  }
}
