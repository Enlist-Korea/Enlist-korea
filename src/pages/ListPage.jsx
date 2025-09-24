// --- import ---
import { useState, useEffect } from "react";
import { Card } from "../components/Card";
import { Loader } from "../components/Loader";
import { fetchRecruitments } from "../api/api";
import { getRecruitmentStatus } from "../utils/dateUtils";

/**
 * useState로 다양한 상태를 관리
 * useEffect를 사용해 컴포넌트가 Mount 될 때 데이터를 가져오고, 필터 값이 바뀔 때마다 리렌더링
 * 가져온 데이터를 map 함수로 순회하여 Card 컴포넌트에 넘겨줘 목록을 렌더링
 * 사용자 입력 (검색, 필터 선택)을 받아 상태를 변경하고, 이에 따라 화면을 동적으로 변경
 */

export const ListPage = () => {
  // --- 상태 관리 ---
  // API 원본 데이터 보관용(originalItems)과 화면 표시용(filteredItems)을 분리.
  // => 필터 초기화 기능을 쉽게 구현하기 위함.
  const [originalItems, setOriginalItems] = useState([]); // API로부터 받은 원본 데이터
  const [filteredItems, setFilteredItems] = useState([]); // 필터링이 적용된 후 화면에 띄울 데이터 목록
  const [isLoading, setIsLoading] = useState(true); // 데이터 로딩 중인지 여부 (초기값: true)
  const [error, setError] = useState(null); // 에러 발생 시 에러 메시지 저장 (초기값: null)
  const [searchTerm, setSearchTerm] = useState(""); // 검색어 상태
  const [selectedForce, setSelectedForce] = // 선택된 군종 필터
    useState("전체 군");
  const [selectedType, setSelectedType] = // 선택된 모집구분 필터
    useState("전체 모집 구분");
  const [selectedStatus, setSelectedStatus] = // 선택된 모집상태 필터
    useState("전체 상태");

  // --- 데이터 로딩 함수 ---
  // localData 함수가 호출 될 때마다 state를 초기화 해줌
  const loadData = async () => {
    setIsLoading(true);
    setError(null);

    try {
      // api.js의 fetchRecruitments 함수를 호출해 데이터 가져옴
      const fetchedItems = await fetchRecruitments();

      // 받아온 데이터에서 '모집중' 상태인 것만 최종적으로 필터링
      // filter 메서드 사용하여 "모집중" 상태인것만 재확인
      // 각각의 item에 대해 getRecruitmentStatus 함수를 실행하여 모집 상태 검사
      const recruitingItems = fetchedItems.filter(
        (item) => {
          const { statusText } = getRecruitmentStatus(
            item.jeopsuSjdtm,
            item.jeopsuJrdtm,
          );
          // 변경된 부분: statusText !== "모집마감" -> statusText === "모집중"
          return statusText === "모집중";
        },
      );

      setOriginalItems(recruitingItems); // 모집중인 병과만 추려낸 결과를 기본값으로 설정
      setFilteredItems(recruitingItems); // 모집중인 병과만 추려낸 결과를 화면에 보여줄 데이터로 설정

      /*
      try {
        const fetchedItems = await fetchRecruitments();
        const availableItems = fetchedItems.filter((item) => {
          const { statusText } = getRecruitmentStatus(
            item.jeopsuSjdtm,
            item.jeopsuJrdtm,
          );
          return statusText !== "모집마감";
        });
        setOriginalItems(availableItems);
        setFilteredItems(availableItems);
      try {
        const fetchedItems = await fetchRecruitments();

        // --- 이 부분이 핵심 변경사항 ---
        // '모집마감' 공고를 필터링하는 로직을 임시로 비활성화
        // API에서 받아온 모든 데이터를 원본과 화면 표시용 데이터로 저장
        setOriginalItems(fetchedItems);
        setFilteredItems(fetchedItems);
        // ---------------------------------
      */
    } catch (err) {
      setError("데이터를 불러오는 중 오류가 발생했습니다.");
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  // --- useEffect Hooks ---
  // 컴포넌트가 처음 렌더링을 마친후 한번만 localData 함수 실행
  // Mount 상태가 되었을 때만 실행
  useEffect(() => {
    loadData();
  }, []);

  // 검색어나 필터 값이 변경될 때마다 실행되어 목록을 다시 필터링
  // 리렌더링을 완료한 후 실행
  useEffect(() => {
    let results = [...originalItems]; // 기존 목록 갖고온 후 변수에 저장

    // 검색어 필터링
    if (searchTerm) {
      // 모두 소문자로 변환 후 필터에서 검색한 것만 필터링
      results = results.filter((item) =>
        item.gsteukgiNm
          .toLowerCase()
          .includes(searchTerm.toLowerCase()),
      );
    }
    // 군종 필터링
    if (selectedForce !== "전체 군") {
      results = results.filter(
        (item) => item.gunGbnm === selectedForce,
      );
    }

    // 모집 구분 필터링
    if (selectedType !== "전체 모집 구분") {
      results = results.filter(
        (item) => item.mojipGbnm === selectedType,
      );
    }

    // 모집상태 필터링
    if (selectedStatus !== "전체 상태") {
      results = results.filter((item) => {
        const { statusText } = getRecruitmentStatus(
          item.jeopsuSjdtm,
          item.jeopsuJrdtm,
        );
        return statusText === selectedStatus;
      });
    }
    setFilteredItems(results);
  }, [
    searchTerm,
    selectedForce,
    selectedType,
    selectedStatus,
    originalItems,
  ]);

  // 필터 초기화 핸들러
  // 초기화 버튼을 눌렀을 때 실행되는 코드
  const handleReset = () => {
    setSearchTerm("");
    setSelectedForce("전체 군");
    setSelectedType("전체 모집 구분");
    setSelectedStatus("전체 상태");
  };

  // --- 렌더링 로직 ---
  // 상황에 따라 다른 UI 출력
  const renderContent = () => {
    if (isLoading)
      return (
        <div
          style={{
            minHeight: "50vh",
            display: "grid",
            placeContent: "center",
          }}
        >
          <Loader />
        </div>
      );
    if (error)
      return (
        <div
          style={{
            textAlign: "center",
            padding: "2rem",
            color: "#ff6b6b",
          }}
        >
          {error}
        </div>
      );
    if (filteredItems.length === 0)
      return (
        <div
          style={{ textAlign: "center", padding: "2rem" }}
        >
          검색 결과가 없습니다.
        </div>
      );

    // 최종적으로 필터링된 아이템들을 Card 컴포넌트로 만들어 화면에 뿌려줌
    return (
      <div className="card-grid">
        {filteredItems.map((item) => (
          // props로 전달
          <Card key={item.id} item={item} />
        ))}
      </div>
    );
  };

  // 이하 JSX는 화면의 전체적인 구조 (헤더, 필터, 결과)를 담당
  return (
    <div className="page-container">
      <header className="page-header">
        <h1>병무청 모집병 조회</h1>
        <p>현재 지원 가능한 모집병 공고를 확인하세요.</p>
      </header>

      <div className="filter-controls">
        <div className="search-bar">
          <span className="icon">🔍</span>
          <input
            type="text"
            placeholder="특기명 검색..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
        <div className="filter-actions">
          <select
            value={selectedForce}
            onChange={(e) =>
              setSelectedForce(e.target.value)
            }
          >
            <option>전체 군</option>
            <option>육군</option>
            <option>해군</option>
            <option>공군</option>
            <option>해병</option>
          </select>
          <select
            value={selectedType}
            onChange={(e) =>
              setSelectedType(e.target.value)
            }
          >
            <option>전체 모집 구분</option>
            {[
              ...new Set(
                originalItems.map((item) => item.mojipGbnm),
              ),
            ].map((type) => (
              <option key={type} value={type}>
                {type}
              </option>
            ))}
          </select>
          <select
            value={selectedStatus}
            onChange={(e) =>
              setSelectedStatus(e.target.value)
            }
          >
            <option>전체 상태</option>
            <option>모집중</option>
            <option>모집예정</option>
          </select>
          <button
            className="reset-btn"
            onClick={handleReset}
          >
            🔄 초기화
          </button>
        </div>
      </div>

      <div className="results-header">
        {!isLoading && !error && (
          <span>
            총 {filteredItems.length}개의 모집공고가
            있습니다.
          </span>
        )}
        <span className="refresh-btn" onClick={loadData}>
          🔃 새로고침
        </span>
      </div>

      <main>{renderContent()}</main>
    </div>
  );
};
