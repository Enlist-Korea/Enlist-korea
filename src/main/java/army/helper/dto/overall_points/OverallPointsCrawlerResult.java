package army.helper.dto.overall_points;

import army.helper.dto.overall_points.detail.AcademicListResponse;
import army.helper.dto.overall_points.detail.AttendanceListResponse;
import army.helper.dto.overall_points.detail.BonusListResponse;
import army.helper.dto.overall_points.detail.QualificationListResponse;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.jsoup.nodes.Attributes;

@Getter
@Setter
public class OverallPointsCrawlerResult {
    private List<AttendanceListResponse> attendanceList = new ArrayList<>();
    private List<QualificationListResponse> qualificationList = new ArrayList<>();
    private List<AcademicListResponse> academicList = new ArrayList<>();
    private List<BonusListResponse> bonusList= new ArrayList<>();

}
