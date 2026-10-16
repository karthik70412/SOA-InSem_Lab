package com.klu.studentservices.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.klu.studentservices.model.Student;

public interface StudentRepository extends JpaRepository<Student, Integer> {

}