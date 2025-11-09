export default function getKoreanForceName(englishForceName) {
  const FORCE_NAME_MAP = {
    ARMY: '육군',
    NAVY: '해군',
    AIR: '공군',
    MARINES: '해병대',
  };

  return FORCE_NAME_MAP[englishForceName] || englishForceName;
}
