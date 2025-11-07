import { useParams, useSearchParams } from 'react-router-dom';
import { useState } from 'react';
import styles from '../css/DetailPage.module.css';
import useSpecialtyData from '../hooks/useSpecialtyData';

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
  const name = searchParams.get('name') || '특기 정보'; // URL에서 ?name= 값

  // 현재 활성화된 탭 ID
  const [activeTab, setActiveTab] = useState('mission');

  // 커스텀 Hook으로 데이터 로직 처리
  const { loading, error, data: specialtyData } = useSpecialtyData(id);

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
          type="button"
          className={`${styles.tabButton} ${
            activeTab === 'mission' ? styles.active : ''
          }`}
          onClick={() => setActiveTab('mission')}
        >
          군사특기임무 및 설명
        </button>
        <button
          type="button"
          className={`${styles.tabButton} ${
            activeTab === 'plan' ? styles.active : ''
          }`}
          onClick={() => setActiveTab('plan')}
        >
          이달의 모집 계획
        </button>
        <button
          type="button"
          className={`${styles.tabButton} ${
            activeTab === 'notice' ? styles.active : ''
          }`}
          onClick={() => setActiveTab('notice')}
        >
          공지사항
        </button>
      </nav>

      <div className={styles.tabContentWrapper}>
        {/* 탭 1: 군사특기임무 */}
        <div
          className={`${styles.tabPane} ${
            activeTab === 'mission' ? styles.active : ''
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
            activeTab === 'plan' ? styles.active : ''
          }`}
        >
          <div
            dangerouslySetInnerHTML={{
              __html:
                specialtyData.planHtml || '<p>모집 계획 정보가 없습니다.</p>',
            }}
          />
        </div>

        {/* 탭 3: 공지사항 */}
        <div
          className={`${styles.tabPane} ${
            activeTab === 'notice' ? styles.active : ''
          }`}
        >
          <div
            dangerouslySetInnerHTML={{
              __html:
                specialtyData.noticeHtml || '<p>공지사항 정보가 없습니다.</p>',
            }}
          />
        </div>
      </div>
    </div>
  );
}
