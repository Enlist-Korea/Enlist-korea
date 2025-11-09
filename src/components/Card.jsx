import { Link } from 'react-router-dom';
import { formatDate, formatYearMonth } from '../utils/dateUtils';
import styles from '../css/Card.module.css';

// --- 1. 메인 Card 컴포넌트 (Default Export) ---
// 오직 조립(Composition)의 책임만 가짐
// item 객체를 받아 하위 컴포넌트에 필요한 데이터만 분배

export default function Card({ item }) {
  const {
    branch,
    mojipGbnm,
    status,
    daysRemainingText,
    name,
    rate,
    quota,
    jeopsuPcnt,
    applyStart,
    applyEnd,
    enlistStart,
    code,
  } = item;

  return (
    <div className={styles.modernCardWrapper}>
      <div className={styles.modernCard}>
        {/* 헤더 로직 분리 */}
        <CardHeader branch={branch} mojipGbnm={mojipGbnm} statusText={status} />

        {/* 제목 */}
        <h3 className={styles.cardTitle}>{name}</h3>

        {/* 본문 로직 분리 */}
        <CardBody
          daysRemainingText={daysRemainingText}
          rate={rate}
          quota={quota}
          jeopsuPcnt={jeopsuPcnt}
          applyStart={applyStart}
          applyEnd={applyEnd}
          enlistStart={enlistStart}
        />

        {/* 푸터 로직 분리 */}
        <CardFooter code={code} name={name} />
      </div>
    </div>
  );
}

// --- 2. 하위 전문 컴포넌트들 ---

/**
 * 카드 헤더: 카테고리와 모집 상태 태그를 렌더링
 * 'getTagClassName' 로직을 캡슐화
 */
function CardHeader({ branch, mojipGbnm, statusText }) {
  const getTagClassName = () => {
    if (statusText === '모집중') return styles.recruiting;
    if (statusText === '모집마감') return styles.finished;
    return styles.upcoming;
  };

  return (
    <div className={styles.cardHeader}>
      <div className={styles.cardCategory}>
        <span>{branch}</span>
        <span>|</span>
        <span>{mojipGbnm}</span>
      </div>
      <span className={`${styles.statusTag} ${getTagClassName()}`}>
        {statusText}
      </span>
    </div>
  );
}

/**
 * 카드 본문: 핵심 스탯, 경쟁률 바, 상세 정보를 조립
 */
function CardBody({
  daysRemainingText,
  rate,
  quota,
  jeopsuPcnt,
  applyStart,
  applyEnd,
  enlistStart,
}) {
  return (
    <div className={styles.cardBody}>
      {/* 핵심 스탯 */}
      <CardKeyStats daysRemainingText={daysRemainingText} rate={rate} />

      {/* 경쟁률 바 로직 분리 */}
      <CardCompetitionBar rate={rate} />

      {/* 상세 정보 그리드 로직 분리 */}
      <CardDetailsGrid
        quota={quota}
        jeopsuPcnt={jeopsuPcnt}
        applyStart={applyStart}
        applyEnd={applyEnd}
        enlistStart={enlistStart}
      />
    </div>
  );
}

/**
 * 핵심 스탯 (남은 기간, 경쟁률)
 */
function CardKeyStats({ daysRemainingText, rate }) {
  return (
    <div className={styles.keyStats}>
      <div className={styles.statItem}>
        <span className={styles.statValue}>{daysRemainingText}</span>
        <span className={styles.statLabel}>남은 기간</span>
      </div>
      <div className={styles.statItem}>
        <span className={styles.statValue}>
          {(Number(rate) || 0).toFixed(1)}
        </span>
        <span className={styles.statLabel}>경쟁률</span>
      </div>
    </div>
  );
}

/**
 * 경쟁률 프로그레스 바:
 * rate를 받아 progressBarWidth를 스스로 계산하고 렌더링
 */
function CardCompetitionBar({ rate }) {
  const competitionRate = rate || 0;
  const progressBarWidth = Math.min((competitionRate / 50) * 100, 100);

  return (
    <div className={styles.competitionBar}>
      <div
        className={styles.competitionBarInner}
        style={{ width: `${progressBarWidth}%` }}
      />
    </div>
  );
}

/**
 * 상세 정보 그리드:
 * 날짜 포맷팅 로직을 캡슐화
 */
function CardDetailsGrid({
  quota,
  jeopsuPcnt,
  applyStart,
  applyEnd,
  enlistStart,
}) {
  return (
    <div className={`${styles.detailsGrid}${styles.fullWidth}`}>
      <DetailItem label="선발" value={`${quota}명`} />
      <DetailItem label="접수" value={`${jeopsuPcnt}명`} />
      <DetailItem
        label="접수기간"
        value={`${formatDate(applyStart)} ~ ${formatDate(applyEnd)}`}
      />
      <DetailItem label="입영예정" value={formatYearMonth(enlistStart)} />
    </div>
  );
}

/**
 * 카드 Footer: '자세히 보기' 링크(Link)를 렌더링
 */
function CardFooter({ code, name }) {
  return (
    <div className={styles.cardFooter}>
      <Link className={styles.applyButton} to={`/details/${code}?name=${name}`}>
        자세히 보기
      </Link>
    </div>
  );
}

/**
 * 상세 정보 아이템 (ProgressBar 하단)
 * 재사용 가능
 */
function DetailItem({ label, value }) {
  return (
    <div className={styles.detailItem}>
      <span className={styles.detailLabel}>{label}</span>
      <span className={styles.detailValue}>{value}</span>
    </div>
  );
}
