/* eslint-disable no-console */ // 이 파일에서 console.log 허용
import fs from "fs";
import puppeteer from "puppeteer";
import * as cheerio from "cheerio";
// eslint-disable-next-line
import * as CARWL from "./crawlerConstants.js";

// [추가] 상단 import 부분
import { crawlRecruitPlan } from "./recruitPlanCrawler.js";

// --- 헬퍼 함수들 ---

/**
 * 'YYYY-MM-DD' 형식의 오늘 날짜 문자열을 반환하는 함수
 * @returns {string}
 */
function getCurrentDateString() {
  const today = new Date();
  const year = today.getFullYear();
  const month = (today.getMonth() + 1).toString().padStart(2, "0");
  const day = today.getDate().toString().padStart(2, "0");
  return `${year}-${month}-${day}`;
}

/**
 * Puppeteer 브라우저와 새 페이지를 초기화하는 함수
 * @returns {Promise<{browser: puppeteer.Browser, page: puppeteer.Page}>}
 */
async function initializeBrowser() {
  console.log("🌐 브라우저 시작 중...");
  // 눈에 보이지 않는 크롬 브라우저 실행
  const browser = await puppeteer.launch({
    headless: true,
    args: ["--no-sandbox", "--disable-setuid-sandbox"],
  });
  // 이후 작업을 실행할 새 탭 열기
  const page = await browser.newPage();
  await page.setUserAgent(CARWL.USER_AGENT);
  return { browser, page };
}

/**
 * MMA AJAX 요청에 필요한 헤더 객체를 생성하는 함수
 * @param {puppeteer.Page} page - 세션 쿠키를 가져올 페이지 객체
 * @param {string} refererUrl - Referer 헤더에 사용할 URL
 * @returns {Promise<Object>} AJAX 헤더 객체
 */
async function createAjaxHeaders(page, refererUrl) {
  const cookies = await page.cookies();
  const cookieHeader = cookies.map((c) => `${c.name}=${c.value}`).join("; ");

  return {
    "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8",
    "X-Requested-With": "XMLHttpRequest",
    "User-Agent": CARWL.USER_AGENT,
    Referer: refererUrl,
    Origin: CARWL.MMA_BASE_URL,
    Cookie: cookieHeader,
  };
}

/**
 * 병무청의 'mc' 세션을 전환하는 함수
 * @param {puppeteer.Page} page - Puppeteer 페이지 객체
 * @param {Object} headers - AJAX 헤더
 * @param {string} mcCode - 전환할 'mc' 코드 (예: 'usr0000127')
 */
async function switchMmaSession(page, headers, mcCode) {
  console.log(`🔄 '${mcCode}' 세션으로 전환 중...`);
  await page.evaluate(
    async (url, evalHeaders, code) => {
      const params = new URLSearchParams({ mc: code });
      await fetch(url, {
        method: "POST",
        headers: evalHeaders,
        body: params.toString(),
        credentials: "same-origin",
      });
    },
    CARWL.MMA_LINK.MENU_SESSION_URL,
    headers,
    mcCode,
  );
  console.log(`✅ '${mcCode}' 세션 등록 완료.`);
}

// --- mmaData.json 크롤링 함수 ---

/**
 * 병무청 특기 목록(JSON)을 fetch API로 가져오는 함수
 * @param {puppeteer.Page} page
 * @param {Object} headers
 * @returns {Promise<Array>} 특기 목록 (예: [{ gsteukgi_cd: '111', ... }])
 */
async function fetchSpecialtyList(page, headers) {
  console.log("📥 병무청 특기 목록(JSON) 요청 중...");
  const listResponse = await page.evaluate(
    async (url, evalHeaders, mcCode) => {
      const params = new URLSearchParams({
        gun_gbcd: "1",
        mojip_gbcd: "1",
        mc: mcCode,
      });
      const res = await fetch(url, {
        method: "POST",
        headers: evalHeaders,
        body: params.toString(),
        credentials: "same-origin",
      });
      return res.text();
    },
    CARWL.MMA_LINK.DUTY_LIST_URL,
    headers,
    CARWL.MC_CODE.SPECIALTY,
  );

  try {
    const listData = JSON.parse(listResponse);
    return listData.list || [];
  } catch {
    console.error("❌ 병무청 특기 목록 파싱 실패:", listResponse.slice(0, 500));
    return [];
  }
}

/**
 * 개별 특기의 상세 정보(JSON)를 fetch API로 가져는 함수
 * @param {puppeteer.Page} page
 * @param {Object} headers
 * @param {string} specialtyId - 특기 ID (gsteukgi_cd)
 * @returns {Promise<Object | null>} 특기 상세 정보 (resultVO)
 */
async function fetchSpecialtyDetail(page, headers, specialtyId) {
  // 서버에 '특기 목록'을 전부 달라고 JSON 데이터를 요청
  // 순차 실행을 위해 await는 필수. ESLint 규칙 비활성화
  // eslint-disable-next-line no-await-in-loop
  const raw = await page.evaluate(
    async (url, evalHeaders, code) => {
      const params = new URLSearchParams({
        gsteukgi_cd: code,
        gun_gbcd: "1",
        mojip_gbcd: "1",
      });
      const res = await fetch(url, {
        method: "POST",
        headers: evalHeaders,
        body: params.toString(),
        credentials: "same-origin",
      });
      return res.text();
    },
    CARWL.MMA_LINK.DUTY_VIEW_URL,
    headers,
    specialtyId,
  );

  const parsed = JSON.parse(raw);
  return parsed.resultVO || null;
}

/**
 * 특기 상세 데이터를 HTML 문자열로 포맷팅하는 함수
 * @param {Object} data - 특기 상세 정보 (resultVO)
 * @returns {string} HTML 문자열
 */
function formatSpecialtyHtml(data) {
  const imageUrl = data.sjgubun_nm
    ? `${CARWL.MMA_BASE_URL}${data.sjgubun_nm}`
    : null;
  return `
<div style="line-height:1.7; font-family:sans-serif;">
  ${
    imageUrl
      ? `<img src="${imageUrl}" alt="특기 이미지" style="max-width:400px; border-radius:8px; margin:1rem 0;">`
      : ""
  }
  <p><b>병과:</b> ${data.mjbunya || "-"}</p>
  <p><b>직무개요 및 임무:</b><br/>${(data.immu_cn || "정보 없음").replace(
    /\r?\n/g,
    "<br/>",
  )}</p>
  <p><b>지원자격:</b><br/>${(data.jwjagyeok_cn || "정보 없음").replace(
    /\r?\n/g,
    "<br/>",
  )}</p>
  <p><b>관련 전공분야:</b> ${data.grbyjikjeop_nm || "-"}</p>
  <p><b>관련 자격분야:</b> ${data.grbyganjeop_nm || "-"}</p>
  <p><b>신체조건:</b><br/>${(data.scjogeon_cn || "").replace(
    /\r?\n/g,
    "<br/>",
  )}</p>
  <p><b>선발 과정:</b><br/>${(data.gita_cn || "").replace(
    /\r?\n/g,
    "<br/>",
  )}</p>
</div>`;
}

/**
 * 모든 병무청 특기 정보를 크롤링하여 JSON 파일로 저장하는 함수
 * (crawlAllMma의 메인 로직)
 * @param {puppeteer.Page} page
 * @param {Object} headers
 */
async function crawlAllMma(page, headers) {
  const specialties = await fetchSpecialtyList(page, headers);
  console.log(`✅ ${specialties.length}개 특기 목록 확인됨`);
  if (specialties.length === 0) {
    console.error(
      "❌ 특기 목록이 비어 있습니다. 병무청 구조가 변경되었을 수 있습니다.",
    );
    return;
  }

  const results = [];
  const lastUpdateDay = getCurrentDateString();

  // 목록을 수신 후, 특기 개수만큼 반복문을 실행
  // for...of는 순차 실행을 위해 필수. ESLint 규칙 비활성화
  // eslint-disable-next-line no-restricted-syntax
  for (const item of specialties) {
    const id = item.gsteukgi_cd;
    const name = item.gtcd_nm;
    console.log(`📄 ${name} (${id}) 불러오는 중...`);

    try {
      // 루프 안에서 '특기 임무'의 JSON으로 된 상세 데이터 요청
      // eslint-disable-next-line
      const data = await fetchSpecialtyDetail(page, headers, id);
      if (data) {
        // 받은 JSON을 HTML로 가공
        const html = formatSpecialtyHtml(data);
        results.push({ id, name, html, lastUpdateDay });
        console.log(`✅ ${name} 저장 완료`);
      }
    } catch (err) {
      console.warn(`⚠️ ${name} (${id}) 실패: ${err.message}`);
    }
  }

  // 모은 모든 데이터를 mmaData.json 파일로 저장
  fs.writeFileSync(
    CARWL.PATH.MMA_DATA_OUT,
    JSON.stringify(results, null, 2),
    "utf8",
  );
  console.log(
    `💾 ${results.length}개 특기 데이터 저장 완료 → ${CARWL.PATH.MMA_DATA_OUT}`,
  );
}

// --- mmaNotices.json 크롤링 함수들 ---

/**
 * 공지사항 목록 HTML을 Cheerio로 파싱하여 공지사항 배열을 반환하는 함수
 * @param {string} htmlText - 파싱할 HTML 텍스트
 * @returns {Array<Object>} 공지사항 객체 배열
 */
function parseNoticesFromHtml(htmlText) {
  // 복사해 온 HTML 덩어리를 펼쳐놓음
  const $ = cheerio.load(htmlText);
  const notices = [];

  // 게시판의 각 줄을 탐색하여, '제목', '날짜', '링크'만 추출
  $("table.board_notice tbody tr").each((i, el) => {
    const row = $(el);
    const headerCell = row.find('th[scope="row"]').first();
    const headerText = headerCell.text().trim();

    // "공지" 헤더는 건너뜀
    if (headerText === "공지") {
      return;
    }

    const titleElement = row.find("td.text_left a");
    const title = titleElement.text().trim();
    const url = titleElement.attr("href");
    const date = row.find("td").eq(2).text().trim();
    const noticeId = headerText; // 실제 게시물 번호

    if (title && url && date && date.match(/^\d{4}-\d{2}-\d{2}$/)) {
      notices.push({
        id: noticeId,
        title,
        url: `${CARWL.MMA_BASE_URL}/board/${url}`,
        date,
      });
    }
  });
  return notices;
}

/**
 * 병무청 육군 공지사항을 크롤링하여 JSON 파일로 저장하는 함수
 * (crawlNoticeBoard의 메인 로직)
 * @param {puppeteer.Page} page - 세션 쿠키가 저장된 Puppeteer 페이지 객체
 */
async function crawlNoticeBoard(page) {
  console.log("📰 육군 공지사항 목록(HTML) 페이지 이동 중... (mc=usr0000127)");
  const noticeUrlWithParams = `${CARWL.MMA_LINK.NOTICE_LIST_URL}?gesipan_id=69&mc=${CARWL.MC_CODE.NOTICE}`;

  let htmlText;
  let pageTitle = "";

  try {
    // 데이터를 몰래 빼오는 게 아닌, 공지사항 게시판 페이지로 이동
    // 공지사항은 내부 함수 호출이 아닌 노출되어 있는 구조
    await page.goto(noticeUrlWithParams, {
      waitUntil: "domcontentloaded",
      timeout: 60000,
    });
    await page.waitForSelector("table.board_notice tbody tr", {
      timeout: 10000,
    });
    console.log("✅ 공지사항 테이블 로딩 감지됨");

    // 현재 보고 있는 페이지의 HTML 전체를 복사
    htmlText = await page.content();
    pageTitle = await page.title();
    console.log(`🔎 현재 페이지 제목: ${pageTitle}`);
  } catch (err) {
    console.error(`페이지 이동 또는 테이블 대기 실패: ${err.message}`);
    return;
  }

  // Cheerio(HTML 전문 분석 도구) 사용
  const notices = parseNoticesFromHtml(htmlText);

  // 뽑아낸 데이터를 mmaNotices.json 파일로 저장
  fs.writeFileSync(
    CARWL.PATH.MMA_NOTICE_OUT,
    JSON.stringify(notices, null, 2),
    "utf8",
  );
  console.log(
    `💾 ${notices.length}개 공지사항 저장 완료 → ${CARWL.PATH.MMA_NOTICE_OUT}`,
  );

  if (notices.length === 0 && pageTitle.includes("무제문서")) {
    fs.writeFileSync("debug_notice.html", htmlText, "utf8");
    console.log("(디버깅용) 로드된 HTML을 debug_notice.html로 저장");
  }
}

/**
 * 메인 실행 함수
 * Puppeteer 브라우저를 설정하고 두 크롤러를 순차적으로 실행하는 함수
 */
async function main() {
  let browser;
  try {
    // 1. 브라우저 초기화
    const { browser: newBrowser, page } = await initializeBrowser();
    browser = newBrowser; // 'finally' 블록에서 참조할 수 있도록 할당

    // 2. 공통 세션 및 헤더 초기화 (특기정보 페이지 기준)
    const mainSessionParams = new URLSearchParams({
      mc: CARWL.MC_CODE.SPECIALTY,
      gun_gbcd: "1",
      mojip_gbcd: "1",
    });
    const mainUrlWithParams = `${CARWL.MMA_LINK.MAIN_SESSION_URL}?${mainSessionParams.toString()}`;

    /**
     * 로봇(page)을 시켜서 병무청의 `육군 특기` 메인 페이지로 이동
     * 바로 직접 데이터 페이지로 접속하면 오류 발생
     * 따라서 메인 페이지 접속 후 쿠키(출입증)를 발급받기 위한 과정
     */
    await page.goto(mainUrlWithParams, {
      waitUntil: "networkidle2",
      timeout: 60000,
    });

    // 방금 받은 쿠키를 포함한 앞으로 데이터를 요청할 때 사용할 AJAX Headers를 생성
    // User-Agent와 Referer 등의 정보 포함
    const ajaxHeaders = await createAjaxHeaders(page, mainUrlWithParams);

    // 첫 번째 크롤러 실행 (특기정보)
    // 이때부터 실행되는 요청은 '특기 정보'에 관한 것
    await switchMmaSession(page, ajaxHeaders, CARWL.MC_CODE.SPECIALTY);
    await crawlAllMma(page, ajaxHeaders);
    // [추가] 3. 이달의 모집계획 크롤링 실행
    // 모집계획은 세션(mc코드)에 덜 민감하므로 공지사항 이후나 별도로 실행해도 무방합니다.
    await crawlRecruitPlan(page);

    // 두 번째 크롤러 실행 (공지사항)
    await switchMmaSession(page, ajaxHeaders, CARWL.MC_CODE.NOTICE);
    // Referer 헤더를 공지사항 페이지로 이동하기 전의 Referer로 설정
    await page.setExtraHTTPHeaders({
      Referer: mainUrlWithParams,
    });
    await crawlNoticeBoard(page);

    console.log("✅ 모든 크롤링 작업 완료.");
  } catch (error) {
    console.error("크롤링 중 심각한 오류 발생:", error);
    process.exitCode = 1; // GitHub Actions 등에서 실패로 처리되도록 설정
  } finally {
    // 5. 브라우저 종료 (성공/실패 여부와 관계없이 항상 실행)
    if (browser) {
      await browser.close();
      console.log("브라우저 종료.");
    }
  }
}

// 스크립트 실행
main();
