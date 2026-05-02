package com.dbsight.neo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "savedquery", schema = "usercredentials")
@Data
public class SavedQueryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "query")
    private String query;
    @Column(name = "title")
    private String title;
    @Column(name = "description")
    private String description;
    @Column(name = "databaseName")
    private String databaseName;
    @Column(name = "userId")
    private Long userId;
}
