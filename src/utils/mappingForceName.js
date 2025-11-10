/**
 * 백엔드에서 받은 영문 군종명을 한글로 변환
 * @param {string} englishForceName - (예: "ARMY", "AIR", "NAVY")
 * @returns {string} (예: "육군", "공군")
 */
export default function getKoreanForceName(englishForceName) {
  const FORCE_NAME_MAP = {
    ARMY: '육군',
    NAVY: '해군',
    AIR: '공군',
    MARINE: '해병대',
  };

  return FORCE_NAME_MAP[englishForceName] || englishForceName;
}
