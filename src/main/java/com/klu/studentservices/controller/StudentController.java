package com.klu.studentservices.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.klu.studentservices.model.Student;
import com.klu.studentservices.service.StudentService;

@RestController
public class StudentController {

    @Autowired
    public StudentService ss;

    @GetMapping("/Student")
    public Student getStudent() {
        return ss.getStudent();
    }

    @PostMapping("/Student")
    public Student saveStudent(@RequestBody Student student) {
        return ss.saveStudent(student);
    }

    @GetMapping("/Student/{id}")
    public Student getStudentById(@PathVariable int id) {
        return ss.getStudentById(id);
    }

    @GetMapping("/Student/all")
    public List<Student> getAllStudents() {
        return ss.getAllStudents();
    }

    @DeleteMapping("/Student/{id}")
    public String deleteStudent(@PathVariable int id) {
        ss.deleteStudent(id);
        return "Student deleted successfully";
    }
}