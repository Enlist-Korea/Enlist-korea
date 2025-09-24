// --- import ---
import { XMLParser } from "fast-xml-parser";
import { getRecruitmentStatus } from "../utils/dateUtils";

/*
 * 외부 API와의 통신을 담당
 * 여러 페이지로 나뉜 데이터를 순회하며 모두 가져오는 로직을 처리
 * XML 형식의 데이터를 JavaScript 객체로 파싱
 * getRecruitemStatus 유틸 함수를 사용해 "모집중"인 데이터만 필터링하여 반환
 */

// API 요청을 보낼 기본 URL과 개인 인증키
// 관리 용이하게 하기 위하여 상수로 선언
const API_BASE_URL = "/api/1300000/MJBGJWJeopSuHH4/list";
const SERVICE_KEY =
  "f74462b64e89c4a27846d3e86dec2bfcc0d6f99428fc89523a70ba5aee0fbe60";

// 비동기 함수로 호출하여 API에서 데이터를 가져오는 작업 시작
export const fetchRecruitments = async () => {
  const MAX_PAGES_TO_SEARCH = 200; // API가 수백 페이질 수 있기에, 우선 200페이지 만큼 탐색
  let allRecruitingItems = []; // "모집중"인 모든 공고를 담을 빈 배열

  // 1 ~ 최대 페이지까지 반복하면서 데이터를 가져옴
  for (
    let pageNo = 1;
    pageNo <= MAX_PAGES_TO_SEARCH;
    pageNo++
  ) {
    console.log(
      `🔍 ${pageNo} 페이지에서 '모집중' 공고 탐색 중...`,
    );
    const url = `${API_BASE_URL}?serviceKey=${SERVICE_KEY}&pageNo=${pageNo}&numOfRows=300`;

    try {
      //fetch 함수를 사용하여 API에 GET 요청을 전송
      const response = await fetch(url);

      // 응답이 성공적으로 오지 않으면 오류 메시지 출력
      if (!response.ok) {
        console.error(
          `HTTP 에러! 상태: ${response.status}`,
        );
        continue;
      }

      // 응답 본문을 텍스트(XML) 형태로 읽어옴
      const xmlText = await response.text();
      // XMLParser 라이브러리를 사용해 XML 텍스트를 JavaScript 객체로 변환
      const parser = new XMLParser();
      const jsonObj = parser.parse(xmlText);
      // 실제 데이터가 있는 경로로 접근
      const items = jsonObj.response?.body?.items?.item;

      // 해당 페이지에 데이터가 없으면 경고 출력 후 다음 페이지로 이동
      if (!items) {
        console.warn(
          `${pageNo} 페이지에 데이터가 없습니다. 다음 페이지로 넘어갑니다.`,
        );
        continue;
      }

      // imtes가 무슨 형태인지 모르기에 삼항 연산자를 이용하여 항상 배열 형태로 변환
      const itemsArray = Array.isArray(items)
        ? items
        : [items];

      // 현재 페이지에서 '모집중'인 공고만 필터링
      const recruitingOnThisPage = itemsArray.filter(
        (item) => {
          // dateUtils 컴포넌트의 함수를 사용해 접수 시작일과 종료일로 현재 상태 계산
          const { statusText } = getRecruitmentStatus(
            item.jeopsuSjdtm,
            item.jeopsuJrdtm,
          );
          return statusText === "모집중";
        },
      );

      // '모집중'인 공고를 찾았다면, 전체 목록에 추가
      if (recruitingOnThisPage.length > 0) {
        console.log(
          `✅ ${pageNo} 페이지에서 '모집중' 공고 ${recruitingOnThisPage.length}개 발견!`,
        );
        allRecruitingItems = [
          ...allRecruitingItems,
          ...recruitingOnThisPage,
        ];
      }

      // API가 제공하는 데이터의 끝에 도달하면 탐색 조기 종료
      const totalCount = jsonObj.response?.body?.totalCount;
      const numOfRows = jsonObj.response?.body?.numOfRows;
      if (
        totalCount &&
        numOfRows &&
        pageNo * numOfRows >= totalCount
      ) {
        console.log("모든 페이지 탐색 완료.");
        break;
      }
    } catch (error) {
      console.error(
        `${pageNo} 페이지를 가져오는 데 실패했습니다:`,
        error,
      );
      continue;
    }
  }

  if (allRecruitingItems.length === 0) {
    console.warn(
      "최대 페이지까지 탐색했지만 모집중인 공고를 찾지 못했습니다.",
    );
  }

  // 루프가 모두 끝난 후, 수집된 '모집중' 공고 전체를 반환
  return allRecruitingItems.map((item) => ({
    ...item,
    id: item.rnum,
  }));
};
