import { useState, useEffect } from "react";
import { Card } from "../components/Card";
import { Loader } from "../components/Loader";
import { fetchRecruitments } from "../api/api";
import { getRecruitmentStatus } from "../utils/dateUtils";

/**
 * [ListPage 컴포넌트]
 * useState로 다양한 상태를 관리
 * useEffect를 사용해 컴포넌트가 Mount 될 때 데이터를 가져오고, 필터 값이 바뀔 때마다 리렌더링
 * 가져온 데이터를 map 함수로 순회하여 Card 컴포넌트에 넘겨줘 목록을 렌더링
 * 사용자 입력 (검색, 필터 선택)을 받아 상태를 변경하고, 이에 따라 화면을 동적으로 변경
 */

export const ListPage = () => {
  // --- 상태 관리 ---
  // API 원본 데이터 보관용(originalItems)과 화면 표시용(filteredItems)을 분리
  // => 필터 초기화 기능을 쉽게 구현하기 위함

  // API로부터 받은 원본 데이터를 보관 (필터 초기화를 위함)
  const [originalItems, setOriginalItems] = useState([]);

  // 사용자 필터가 적용된, 화면에 실제 표시될 데이터를 보관
  const [filteredItems, setFilteredItems] = useState([]);

  // 데이터 로딩 중인지 여부를 표시 (초기값: true)
  const [isLoading, setIsLoading] = useState(true);

  // 에러 발생 시 에러 메시지 저장 (초기값: null)
  const [error, setError] = useState(null);

  // 사용자가 검색한 검색어 (초기값: 빈 문자열)
  const [searchTermFilteredValue, setSearchTerm] =
    useState("");
  // 사용자가 선택한 '군종'
  const [selectedForce, setSelectedForce] =
    useState("전체 군");
  // 사용자가 선택한 '모집 구분'
  const [selectedType, setSelectedType] =
    useState("전체 모집 구분");
  // 사용자가 선택한 '모집 상태'
  const [selectedStatus, setSelectedStatus] =
    useState("전체 상태");

  /**
   * 컴포넌트가 첫 렌더링 될 때 최초 1행 실행하는 데이터 로딩 함수
   * 비동기
   */
  const onceLoadData = async () => {
    setIsLoading(true);
    setError(null);

    try {
      // api.js의 fetchRecruitments 함수를 호출해 데이터 가져옴
      const fetchedItems = await fetchRecruitments(); // 받아온 데이터에서 '모집마감' 상태인 것만 제외

      // 받아온 데이터에서 '모집 마감' 상태인 공고는 제외시킴
      const availableItems = fetchedItems.filter((item) => {
        const { statusText } = getRecruitmentStatus(
          item.jeopsuSjdtm,
          item.jeopsuJrdtm,
        );
        return statusText !== "모집마감";
      });

      // 원본 데이터와 화면 표시용 데이터 상태를 업데이트
      setOriginalItems(availableItems); // 원본 데이터 설정
      setFilteredItems(availableItems); // 화면 표시용 데이터도 최초는 동일하게 생성
    } catch (err) {
      // 에러 발생 시
      setError("데이터를 불러오는 중 오류가 발생했습니다.");
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  // --- 이벤트 Hooks ---

  /**
   * 컴포넌트가 처음 마운트될 때 loadData() 함수 1회 실행
   */
  useEffect(() => {
    onceLoadData();
  }, []);

  /**
   * 사용자가 필터 관련 상태를 변경할 때마다 실행
   * `originalItems` 배열을 기반으로 필터링을 수행하고, 'filteredItems' 상태를 업데이트
   */
  useEffect(() => {
    // 원본 데이터 배열을 스프레드 연산자를 활용하여 복사 후 사용
    let filterSearchResults = [...originalItems];

    // 검색어 필터링
    if (searchTermFilteredValue) {
      // 모두 소문자로 변환 후 필터에서 검색한 것만 필터링
      filterSearchResults = filterSearchResults.filter(
        (item) =>
          item.gsteukgiNm
            .toLowerCase()
            .includes(
              searchTermFilteredValue.toLowerCase(),
            ),
      );
    }

    // 군종 필터링
    if (selectedForce !== "전체 군") {
      filterSearchResults = filterSearchResults.filter(
        (item) => item.gunGbnm === selectedForce,
      );
    }

    // 모집 구분 필터링
    if (selectedType !== "전체 모집 구분") {
      filterSearchResults = filterSearchResults.filter(
        (item) => item.mojipGbnm === selectedType,
      );
    }

    // 모집상태 필터링
    if (selectedStatus !== "전체 상태") {
      filterSearchResults = filterSearchResults.filter(
        (item) => {
          const { statusText } = getRecruitmentStatus(
            item.jeopsuSjdtm,
            item.jeopsuJrdtm,
          );
          return statusText === selectedStatus;
        },
      );
    }
    setFilteredItems(filterSearchResults);
  }, [
    searchTermFilteredValue,
    selectedForce,
    selectedType,
    selectedStatus,
    originalItems,
  ]);

  // 초기화 버튼을 눌렀을 때 실행되는 코드
  const resetButtonHandle = () => {
    setSearchTerm("");
    setSelectedForce("전체 군");
    setSelectedType("전체 모집 구분");
    setSelectedStatus("전체 상태");
  };

  // --- 렌더링 로직 --- // 상황에 따라 다른 UI 출력
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
          <Loader />{" "}
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
          {error}{" "}
        </div>
      );
    if (filteredItems.length === 0)
      return (
        <div
          style={{ textAlign: "center", padding: "2rem" }}
        >
          검색 결과가 없습니다.{" "}
        </div>
      );

    // 최종적으로 필터링된 아이템들을 Card 컴포넌트로 만들어 화면에 뿌려줌
    return (
      <div className="card-grid">
        {" "}
        {filteredItems.map(
          (
            item, // props로 전달
          ) => (
            <Card key={item.id} item={item} />
          ),
        )}{" "}
      </div>
    );
  };

  // 이하 JSX는 화면의 전체적인 구조 (헤더, 필터, 결과)를 담당
  return (
    <div className="page-container">
      {" "}
      <header className="page-header">
        <h1>병무청 모집병 조회</h1>{" "}
        <p>
          현재 지원 가능한 모집병 공고를 확인하세요.
        </p>{" "}
      </header>{" "}
      <div className="filter-controls">
        {" "}
        <div className="search-bar">
          <span className="icon">🔍</span>{" "}
          <input
            type="text"
            placeholder="특기명 검색..."
            value={searchTermFilteredValue}
            onChange={(e) => setSearchTerm(e.target.value)}
          />{" "}
        </div>{" "}
        <div className="filter-actions">
          {" "}
          <select
            value={selectedForce}
            onChange={(e) =>
              setSelectedForce(e.target.value)
            }
          >
            <option>전체 군</option> <option>육군</option>{" "}
            <option>해군</option> <option>공군</option>{" "}
            <option>해병</option>{" "}
          </select>{" "}
          <select
            value={selectedType}
            onChange={(e) =>
              setSelectedType(e.target.value)
            }
          >
            <option>전체 모집 구분</option>{" "}
            {[
              ...new Set(
                originalItems.map((item) => item.mojipGbnm),
              ),
            ].map((type) => (
              <option key={type} value={type}>
                {type}{" "}
              </option>
            ))}{" "}
          </select>{" "}
          <select
            value={selectedStatus}
            onChange={(e) =>
              setSelectedStatus(e.target.value)
            }
          >
            <option>전체 상태</option>
            <option>모집중</option>{" "}
            <option>모집예정</option>{" "}
          </select>{" "}
          <button
            className="reset-btn"
            onClick={resetButtonHandle}
          >
            초기화{" "}
          </button>{" "}
        </div>{" "}
      </div>{" "}
      <div className="results-header">
        {" "}
        {!isLoading && !error && (
          <span>
            총 {filteredItems.length}개의 모집공고가
            있습니다.{" "}
          </span>
        )}{" "}
      </div>
      <main>{renderContent()}</main>{" "}
    </div>
  );
};
