import { useParams, useSearchParams } from "react-router-dom";
import { useState, useEffect } from "react";
import mmaData from "../data/mmaData.json"; // 로컬 JSON 불러오기
import styles from "../css/DetailPage.module.css";

/**
 * [DetailPage 컴포넌트]
 * 탭 상태(activeTab)를 추가로 관리
 * mmaData.json에서 id로 '특기 전체 데이터'를 찾음
 * 탭 버튼 클릭 시 activeTab 상태를 변경하고, 해당 탭에 맞는 데이터를 렌더링
 */
export default function DetailPage() {
  // --- Hooks 및 상태 초기화 ---
  const { id } = useParams(); // URL에서 :id 값 (예: "133.106")
  const [searchParams] = useSearchParams();
  const name = searchParams.get("name") || "특기 정보"; // URL에서 ?name= 값

  // 로딩 상태
  const [loading, setLoadingState] = useState(true);
  // 에러 상태
  const [error, setError] = useState(null);
  // 현재 활성화된 탭 ID (기본값: 'mission')
  const [activeTab, setActiveTab] = useState("mission");
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

  // 로딩 중일 때
  if (loading) {
    return <div className={styles.loadingText}>불러오는 중...</div>;
  }

  // 에러 발생 시
  if (error) {
    return <div className={styles.errorText}>{error}</div>;
  }

  // 데이터가 없을 때
  if (!specialtyData) {
    return (
      <div className={styles.noDataText}>데이터를 표시할 수 없습니다.</div>
    );
  }

  // --- 메인 렌더링 ---
  return (
    <div className={styles.pageContainer}>
      <h1 className={styles.title}>
        {name} ({id})
      </h1>

      <nav className={styles.tabNav}>
        <button
          className={`${styles.tabButton} ${
            activeTab === "mission" ? styles.active : ""
          }`}
          onClick={() => setActiveTab("mission")}
        >
          군사특기임무 및 설명
        </button>
        <button
          className={`${styles.tabButton} ${
            activeTab === "plan" ? styles.active : ""
          }`}
          onClick={() => setActiveTab("plan")}
        >
          이달의 모집 계획
        </button>
        <button
          className={`${styles.tabButton} ${
            activeTab === "notice" ? styles.active : ""
          }`}
          onClick={() => setActiveTab("notice")}
        >
          공지사항
        </button>
      </nav>

      <div className={styles.tabContentWrapper}>
        {/* 탭 1: 군사특기임무 */}
        <div
          className={`${styles.tabPane} ${
            activeTab === "mission" ? styles.active : ""
          }`}
        >
          <div
            dangerouslySetInnerHTML={{
              __html: specialtyData.html,
            }}
          />
        </div>

        {/* 탭 2: 이달의 모집 계획 */}
        <div
          className={`${styles.tabPane} ${
            activeTab === "plan" ? styles.active : ""
          }`}
        >
          <div
            dangerouslySetInnerHTML={{
              __html:
                specialtyData.planHtml || "<p>모집 계획 정보가 없습니다.</p>",
            }}
          />
        </div>

        {/* 탭 3: 공지사항 */}
        <div
          className={`${styles.tabPane} ${
            activeTab === "notice" ? styles.active : ""
          }`}
        >
          <div
            dangerouslySetInnerHTML={{
              __html:
                specialtyData.noticeHtml || "<p>공지사항 정보가 없습니다.</p>",
            }}
          />
        </div>
      </div>
    </div>
  );
}
