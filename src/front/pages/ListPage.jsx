import { useState, useEffect } from 'react';
import Card from '../components/Card';
import Loader from '../components/Loader';
import fetchRecruitments from '../api/api';
import styles from '../css/ListPage.module.css';
import getKoreanForceName from '../utils/mappingForceName';
import { getExpandedKeywords } from '../utils/searchKeywords';

export default function ListPage() {
  // --- 상태 관리 ---
  const [originalItems, setOriginalItems] = useState([]);
  const [filteredItems, setFilteredItems] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  // 필터 상태
  const [searchTermFilteredValue, setSearchTerm] = useState('');
  const [selectedForce, setSelectedBranch] = useState('전체 군');
  const [selectedType, setSelectedRecuritmentType] = useState('전체 모집 구분');
  const [selectedStatus, setSelectedRecruitmentStatus] = useState('전체 상태');

  // 광범위 검색 상태
  const [isBroadSearch, setIsBroadSearch] = useState(false);

  /**
   * 데이터 로딩 함수 (api.js가 변환 및 필터링 담당)
   */
  const loadData = async () => {
    setIsLoading(true);
    setError(null);

    try {
      const fetchedItems = await fetchRecruitments();
      const mappedItems = fetchedItems.map((item) => ({
        ...item,
        branch: getKoreanForceName(item.branch), // 영문 군별 이름매핑 함수 적용
      }));

      setOriginalItems(mappedItems);
      setFilteredItems(mappedItems);
    } catch (err) {
      setError('데이터를 불러오는 중 오류가 발생했습니다.');
      error.message(err);
    } finally {
      setIsLoading(false);
    }
  };

  // --- 이벤트 Hooks ---

  /**
   * 컴포넌트가 처음 마운트될 때 loadData() 함수 1회 실행
   */
  useEffect(() => {
    loadData();
  }, []);

  /**
   * 필터 관련 상태 변경 시 실행
   */
  useEffect(() => {
    let filterSearchResults = [...originalItems];
    const normalizedSearchTerm = searchTermFilteredValue.toLowerCase();

    if (normalizedSearchTerm) {
      // --- 검색 모드에 따라 로직 분기 ---
      // 광범위 검색
      if (isBroadSearch) {
        const keywords = getExpandedKeywords(normalizedSearchTerm);

        filterSearchResults = filterSearchResults.filter((item) => {
          // item의 이름과 직무 설명을 준비
          const nameLower = item.name.toLowerCase();
          const descLower = (item.descriptionText ?? '').toLowerCase(); // null이나 undefined일 경우 빈 배열로 변경 후 toLowerCase()

          return keywords.some(
            (keyword) =>
              nameLower.includes(keyword) || descLower.includes(keyword),
          );
        });
      } else {
        // 기존 검색 로직
        filterSearchResults = filterSearchResults.filter((item) =>
          item.name.toLowerCase().includes(normalizedSearchTerm),
        );
      }
    }
    if (selectedForce !== '전체 군') {
      // 군종 필터링
      filterSearchResults = filterSearchResults.filter(
        (item) => item.branch === selectedForce,
      );
    }

    // 모집 구분 필터링
    if (selectedType !== '전체 모집 구분') {
      filterSearchResults = filterSearchResults.filter(
        (item) => item.mojipGbnm === selectedType,
      );
    }

    // 모집상태 필터링 (api.js가 계산해준 status 필드 사용)
    if (selectedStatus !== '전체 상태') {
      filterSearchResults = filterSearchResults.filter(
        (item) => item.status === selectedStatus,
      );
    }
    setFilteredItems(filterSearchResults);
  }, [
    searchTermFilteredValue,
    selectedForce,
    selectedType,
    selectedStatus,
    originalItems,
    isBroadSearch,
  ]);

  // 초기화 버튼 핸들러
  const resetButtonHandler = () => {
    setSearchTerm('');
    setSelectedBranch('전체 군');
    setSelectedRecuritmentType('전체 모집 구분');
    setSelectedRecruitmentStatus('전체 상태');
  };

  // --- 렌더링 로직 ---
  const renderContent = () => {
    if (isLoading)
      return (
        <div className={styles.loadingContainer}>
          <Loader />
        </div>
      );
    if (error) return <div className={styles.errorText}>{error}</div>;
    if (filteredItems.length === 0)
      return <div className={styles.noResults}>검색 결과가 없습니다.</div>;

    return (
      <div className={styles.cardGrid}>
        {/* Card 컴포넌트에 새 스키마(item)와 고유 id(key) 전달 */}
        {filteredItems.map((item) => (
          <Card key={item.id} item={item} />
        ))}
      </div>
    );
  };

  return (
    <div className={styles.pageContainer}>
      <header className={styles.pageHeader}>
        <h1>병무청 모집병 조회</h1>
        <p>현재 지원 가능한 모집병 공고를 확인하세요.</p>
      </header>

      <div className={styles.filterControls}>
        <div className={styles.searchBar}>
          <span className={styles.icon}>🔍</span>
          <input
            type="text"
            // placeholder="특기명 검색..." // [수정] item.name 기준 검색
            placeholder={
              isBroadSearch
                ? '예: "운전" 또는 "사격" (AI 추천)'
                : '정확한 특기명 검색...'
            }
            value={searchTermFilteredValue}
            onChange={(ele) => setSearchTerm(ele.target.value)}
          />
        </div>

        <div className={`${styles.filterActions} 검색어 입력 & 필터 선택`}>
          {/* [수정] 검색 모드 토글 버튼 className 수정 */}
          <button
            type="button"
            className={`${styles.resetBtn} ${
              isBroadSearch ? styles.aiSearchActive : ''
            }`}
            onClick={() => setIsBroadSearch((prev) => !prev)}
            title={
              isBroadSearch
                ? '특기명, 직무내용, 연관 키워드까지 검색합니다.'
                : '특기명에 포함된 단어만 정확히 검색합니다.'
            }
          >
            검색: {isBroadSearch ? 'AI 추천' : '특기명'}
          </button>

          {/* 군종 필터 */}
          <select
            className={`${styles.filterSelect} 군종 필터`}
            value={selectedForce}
            onChange={(ele) => setSelectedBranch(ele.target.value)}
          >
            <option>전체 군</option>
            <option>육군</option>
            <option>해군</option>
            <option>공군</option>
            <option>해병대</option>
          </select>

          {/* 모집 구분 필터 */}
          <select
            className={`${styles.filterSelect} 모집 구분`}
            value={selectedType}
            onChange={(ele) => setSelectedRecuritmentType(ele.target.value)}
          >
            <option>전체 모집 구분</option>
            {[...new Set(originalItems.map((item) => item.mojipGbnm))].map(
              (type) => (
                <option key={type} value={type}>
                  {type}
                </option>
              ),
            )}
          </select>

          {/* 모집 상태 필터 */}
          <select
            className={`${styles.filterSelect} 모집 상태`}
            value={selectedStatus}
            onChange={(ele) => setSelectedRecruitmentStatus(ele.target.value)}
          >
            <option>전체 상태</option>
            <option>모집중</option>
            <option>모집예정</option>
            {/* '모집마감'은 api.js에서 미리 필터링 */}
          </select>

          <button
            type="button"
            className={styles.resetBtn}
            onClick={resetButtonHandler}
          >
            초기화
          </button>
        </div>
      </div>

      <div className={`${styles.resultsHeader} 모집공고 개수`}>
        {!isLoading && !error && (
          <span>총 {filteredItems.length}개의 모집공고가 있습니다.</span>
        )}
      </div>

      <main>{renderContent()}</main>
    </div>
  );
}
