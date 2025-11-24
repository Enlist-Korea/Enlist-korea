import React from 'react';
import styles from '../css/RecruitPlan.module.css';
// 크롤링한 JSON 데이터 import (경로는 실제 위치에 맞게 조정)
import planData from '../data/recruitPlan.json';

export default function RecruitPlanView() {
  // 데이터가 없을 경우 처리
  if (!planData || planData.length === 0) {
    return (
      <div style={{ padding: '2rem', textAlign: 'center', color: '#888' }}>
        등록된 모집 계획이 없습니다.
      </div>
    );
  }

  return (
    <div className={styles.planGrid}>
      {planData.map((item, index) => (
        <div key={index} className={styles.planCard}>
          {/* 상단: 뱃지 & 모집명 */}
          <div className={styles.cardHeader}>
            <div>
              <span className={styles.badge}>{item.category}</span>
              <h3 className={styles.title}>{item.name}</h3>
            </div>
          </div>

          {/* 중간: 상세 일정 */}
          <div className={styles.infoList}>
            <div className={styles.infoItem}>
              <span className={styles.label}>📅 접수기간</span>
              <span className={styles.value}>{item.period}</span>
            </div>
            <div className={styles.infoItem}>
              <span className={styles.label}>📢 1차발표</span>
              <span className={styles.value}>{item.date_step1}</span>
            </div>
            <div className={styles.infoItem}>
              <span className={styles.label}>🎉 최종발표</span>
              <span className={styles.value}>{item.date_final}</span>
            </div>
          </div>

          {/* 하단: 입영월 & 인원 */}
          <div className={styles.cardFooter}>
            <div className={styles.footerItem}>
              <span className={styles.footerLabel}>입영 시기</span>
              <span className={styles.footerValue} style={{ color: '#fff' }}>
                {item.enlist_month}
              </span>
            </div>
            <div className={styles.footerItem}>
              <span className={styles.footerLabel}>모집 인원</span>
              <span className={styles.footerValue}>{item.count}명</span>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}
