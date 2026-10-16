package com.klu.studentservices.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.klu.studentservices.dto.Faculty;
import com.klu.studentservices.model.Student;
import com.klu.studentservices.repository.StudentRepository;

import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;

@Service
public class StudentService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CircuitBreakerFactory circuitBreakerFactory;

    @Autowired
    private StudentRepository studentRepository;

    public Student getStudent() {

        CircuitBreaker circuitBreaker =
                circuitBreakerFactory.create("facultyService");

        Faculty faculty = circuitBreaker.run(
                () -> restTemplate.getForObject(
                        "http://FACULTY-SERVICE/Faculty",
                        Faculty.class
                ),
                throwable -> facultyFallback()
        );

        Student student =
                studentRepository.findById(501).orElse(null);

        if (student != null) {
            student.setFaculty(faculty);
        }

        return student;
    }

    public Student saveStudent(Student student) {
        return studentRepository.save(student);
    }

    public Student getStudentById(int id) {

        Student student =
                studentRepository.findById(id).orElse(null);

        if (student != null) {

            CircuitBreaker circuitBreaker =
                    circuitBreakerFactory.create("facultyService");

            Faculty faculty = circuitBreaker.run(
                    () -> restTemplate.getForObject(
                            "http://FACULTY-SERVICE/Faculty",
                            Faculty.class
                    ),
                    throwable -> facultyFallback()
            );

            student.setFaculty(faculty);
        }

        return student;
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public void deleteStudent(int id) {
        studentRepository.deleteById(id);
    }

    public Faculty facultyFallback() {

        Faculty faculty = new Faculty();

        faculty.setId(0);
        faculty.setName("Faculty Service Down");
        faculty.setDepartment(null);

        return faculty;
    }
}