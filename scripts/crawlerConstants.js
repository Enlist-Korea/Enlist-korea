/* eslint-disable no-underscore-dangle */
import path from "path";
import { fileURLToPath } from "url";

// --- 경로 설정 ---
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// --- 기본 URL ---
export const MMA_BASE_URL = "https://www.mma.go.kr";
export const CONSCRIPTION_BASE_URL = `${MMA_BASE_URL}/conscription/recruit_service/procedure/army/`;

export const PATH = {
  MMA_DATA_OUT: path.join(
    __dirname,
    "..",
    "src",
    "front",
    "data",
    "mmaData.json",
  ),
  MMA_NOTICE_OUT: path.join(
    __dirname,
    "..",
    "src",
    "front",
    "data",
    "mmaNotices.json",
  ),
};

// --- 병무청 링크 ---
export const MMA_LINK = {
  MAIN_SESSION_URL: `${CONSCRIPTION_BASE_URL}S_board_text.do`,
  DUTY_LIST_URL: `${MMA_BASE_URL}/conscription/army/dutyList.do`,
  DUTY_VIEW_URL: `${MMA_BASE_URL}/conscription/army/dutyView.do`,
  NOTICE_LIST_URL: `${MMA_BASE_URL}/board/boardList.do`,
  MENU_SESSION_URL: `${MMA_BASE_URL}/syjy/selectMenuPersonInCharge.json`,
};

// --- MMA 'mc' 코드
export const MC_CODE = {
  SPECIALTY: "mma0000388",
  NOTICE: "usr0000127",
};

// --- 기타 ---
export const USER_AGENT =
  "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141 Safari/537.36";
