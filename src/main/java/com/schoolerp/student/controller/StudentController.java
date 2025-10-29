package com.schoolerp.student.controller;

import com.schoolerp.student.common.LogContext;
import com.schoolerp.student.common.StandardResponse;
import com.schoolerp.student.dto.*;
import com.schoolerp.student.entity.IdCard;
import com.schoolerp.student.entity.Marksheet;
import com.schoolerp.student.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/student-service/")
@Validated
@Slf4j
public class StudentController {

    @Autowired
    private final StudentServiceImpl studentService;
    @Autowired
    private final IdCardServiceIMPL idCardService;
    @Autowired
    private final MarksheetServiceIMPL marksheetService;

    public StudentController(StudentServiceImpl studentService, IdCardServiceIMPL idCardService, MarksheetServiceIMPL marksheetService) {
        this.studentService = studentService;
        this.idCardService = idCardService;
        this.marksheetService = marksheetService;
    }


    @GetMapping("/getStudentList")
    public ResponseEntity<StandardResponse<StudentsListResponseDto>> getAllStudents(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer status,
            @RequestParam(value = "class", required = false) Integer classFilter,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        
        String logId = LogContext.getLogId();
        log.info("logId: {} - API call: GET /api/students with filters - search: {}, status: {}, class: {}, page: {}, size: {}", 
                 logId, search, status, classFilter, page, size);
        
        StudentsListResponseDto response = studentService.getAllStudents(search, status, classFilter, page, size);
        
        StandardResponse.ResponseMetadata metadata = StandardResponse.ResponseMetadata.builder()
                .totalRecords(response.getTotal())
                .currentPage(page != null ? page : 0)
                .pageSize(size != null ? size : 20)
                .operation("GET_ALL_STUDENTS")
                .build();
        
        return ResponseEntity.ok(StandardResponse.success(response, "Students retrieved successfully", metadata));
    }
    
    @GetMapping("getStudentById/{id}")
    public ResponseEntity<StandardResponse<StudentResponseDto>> getStudentById(@PathVariable Integer id) {
        String logId = LogContext.getLogId();
        log.info("logId: {} - API call: GET /api/students/{}", logId, id);
        
        StudentResponseDto student = studentService.getStudentById(id);
        
        StandardResponse.ResponseMetadata metadata = StandardResponse.ResponseMetadata.builder()
                .operation("GET_STUDENT_BY_ID")
                .build();
        
        return ResponseEntity.ok(StandardResponse.success(student, "Student retrieved successfully", metadata));
    }
    
    @PostMapping("/createStudent")
    public ResponseEntity<StandardResponse<StudentResponseDto>> createStudent(@Valid @RequestBody StudentRequestDto requestDto) {
        String logId = LogContext.getLogId();
        log.info("logId: {} - API call: POST /api/students for: {} {}", logId, requestDto.getFirstName(), requestDto.getLastName());
        
        StudentResponseDto createdStudent = studentService.createStudent(requestDto);
        
        StandardResponse.ResponseMetadata metadata = StandardResponse.ResponseMetadata.builder()
                .operation("CREATE_STUDENT")
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(StandardResponse.success(createdStudent, "Student created successfully", metadata));
    }
    
    @PutMapping("updateStudent/{id}")
    public ResponseEntity<StandardResponse<StudentResponseDto>> updateStudent(
            @PathVariable Integer id,
            @Valid @RequestBody StudentRequestDto requestDto) {
        
        String logId = LogContext.getLogId();
        log.info("logId: {} - API call: PUT /api/students/{}", logId, id);
        
        StudentResponseDto updatedStudent = studentService.updateStudent(id, requestDto);
        
        StandardResponse.ResponseMetadata metadata = StandardResponse.ResponseMetadata.builder()
                .operation("UPDATE_STUDENT")
                .build();
        
        return ResponseEntity.ok(StandardResponse.success(updatedStudent, "Student updated successfully", metadata));
    }
    
    @DeleteMapping("deleteStudent/{id}")
    public ResponseEntity<StandardResponse<Void>> deleteStudent(@PathVariable Integer id) {
        String logId = LogContext.getLogId();
        log.info("logId: {} - API call: DELETE /api/students/{}", logId, id);
        
        studentService.deleteStudent(id);
        
        StandardResponse.ResponseMetadata metadata = StandardResponse.ResponseMetadata.builder()
                .operation("DELETE_STUDENT")
                .build();
        
        return ResponseEntity.ok(StandardResponse.success("Student deleted successfully"));
    }
    
    @PostMapping("/promote")
    public ResponseEntity<StandardResponse<Void>> promoteStudents(@Valid @RequestBody PromoteStudentsRequestDto requestDto) {
        String logId = LogContext.getLogId();
        log.info("logId: {} - API call: POST /api/students/promote for {} students from {}-{} to {}-{}", 
                 logId, requestDto.getStudentIds().size(),
                 requestDto.getFromClass(), requestDto.getFromSection(),
                 requestDto.getToClass(), requestDto.getToSection());
        
        studentService.promoteStudents(requestDto);
        
        StandardResponse.ResponseMetadata metadata = StandardResponse.ResponseMetadata.builder()
                .operation("PROMOTE_STUDENTS")
                .totalRecords((long) requestDto.getStudentIds().size())
                .build();
        
        return ResponseEntity.ok(StandardResponse.success("Students promoted successfully"));
    }
    
    // ID Cards endpoints
    @GetMapping("/id-cards")
    public ResponseEntity<List<IdCard>> getAllIdCards() {
        log.info("Fetching all ID cards");
        
        List<IdCard> idCards = idCardService.getAllIdCards();
        return ResponseEntity.ok(idCards);
    }
    
    @PostMapping("/id-cards")
    public ResponseEntity<IdCard> createIdCard(@Valid @RequestBody IdCardRequestDto requestDto) {
        log.info("Creating ID card for student: {}", requestDto.getStudentId());
        
        IdCard createdIdCard = idCardService.createIdCard(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdIdCard);
    }
    
    // Marksheets endpoints
    @GetMapping("/marksheets")
    public ResponseEntity<List<Marksheet>> getAllMarksheets() {
        log.info("Fetching all marksheets");
        
        List<Marksheet> marksheets = marksheetService.getAllMarksheets();
        return ResponseEntity.ok(marksheets);
    }
    
    @PostMapping("/marksheets")
    public ResponseEntity<Marksheet> createMarksheet(@Valid @RequestBody MarksheetRequestDto requestDto) {
        log.info("Creating marksheet for student: {}", requestDto.getStudentId());
        
        Marksheet createdMarksheet = marksheetService.createMarksheet(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMarksheet);
    }

}
