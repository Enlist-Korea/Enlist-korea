import { useState, useEffect } from 'react';
import mmaData from '../data/mmaData.json';

export default function useSpecialtyData(id) {
  // 로딩 상태
  const [loading, setLoadingState] = useState(true);
  // 에러 상태
  const [error, setError] = useState(null);
  // JSON에서 찾은 특기 데이터 전체 (html, planHtml, noticeHtml 포함)
  const [specialtyData, setSpecialtyData] = useState(null);

  // --- 데이터 로딩 Effect ---
  useEffect(() => {
    setLoadingState(true);
    setError(null);
    setSpecialtyData(null);

    // mmaData에서 URL의 id와 일치하는 항목을 검색
    const foundData = mmaData.find((MOS) => MOS.id === id);

    if (foundData) {
      // 데이터를 찾으면 전체 객체를 상태에 저장
      setSpecialtyData(foundData);
    } else {
      // 데이터가 없으면 에러 메시지 설정
      setError(`ID ${id} 에 해당하는 캐시된 데이터가 없습니다.`);
    }

    setLoadingState(false);
  }, [id]); // `id` 값이 바뀔 때만 재실행

  return { loading, error, data: specialtyData };
}
