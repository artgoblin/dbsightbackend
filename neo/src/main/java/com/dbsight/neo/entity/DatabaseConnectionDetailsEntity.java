package com.dbsight.neo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "databaseconnectiondetails", schema = "usercredentials")
@Getter
@Setter
public class DatabaseConnectionDetailsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "connection_name", nullable = false)
    private String connectionName;
    @Column(name = "dbname", nullable = false)
    private String dbName; 
    @Column(name = "dburl", nullable = false)
    private String dbUrl;
    @Column(name = "dbusername", nullable = false)
    private String dbUsername;
    @Column(name = "dbpassword", nullable = false)
    private String dbPassword;
    @Column(name = "dbtype", nullable = false)
    private String dbType;
    @Column(name = "dbport", nullable = false)
    private String dbPort;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userid")
    private User user;
    
}
