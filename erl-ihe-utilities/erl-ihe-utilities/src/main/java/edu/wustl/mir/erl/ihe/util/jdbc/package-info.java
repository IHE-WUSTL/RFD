/*
 * Copyright (c) 2015  Washington University in St. Louis
 *  All rights reserved. This program and the accompanying 
 *  materials are made available under the terms of the
 *  Apache License, Version 2.0 (the "License");  you may not 
 *  use this file except in compliance with the License.
 * The License is available at:
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, 
 *  software  distributed under the License is distributed on 
 *  an "AS IS" BASIS,  WITHOUT WARRANTIES OR CONDITIONS  
 *  OF ANY KIND, either express or implied. See the License 
 *  for the specific language governing permissions and
 *  limitations under the License.
 *  
 *  Contributors:
 *    Initial author: Ralph Moulton / MIR WUSM IHE Development Project 
 *    moultonr@mir.wustl.edu
 */
/**
 * Utility helper classes and annotations for jdbc processing in ERL IHE
 * software. See the specific JavaDoc for more detailed information:
 * <ul>
 * <li>The {@link edu.wustl.mir.erl.ihe.util.jdbc.RDBMS RDBMS} Enum contains an
 * instance for each Relational Data Base Management System (RDBMS) supported by
 * the jdbc package. On startup, the application invokes
 * {@link edu.wustl.mir.erl.ihe.util.jdbc.JDBC#setSupportedDatabases(RDBMS...)
 * JDBC#setSupportedDatabases} to tell the jdbc package which of these it also
 * supports.</li>
 * <li>The class {@link edu.wustl.mir.erl.ihe.util.jdbc.DBHelper DBHelper} and
 * the annotation {@link edu.wustl.mir.erl.ihe.util.jdbc.Helper Helper}:
 * <ul>
 * <li>Automate the generation of inner classes implementing the
 * {@link java.util.Comparator Comparator} interface, allowing arrays of class
 * instances to be sorted on any reasonable property of the class. This is
 * especially useful in web applications using tables corresponding to arrays of
 * class instances.</li>
 * <li>Allows the use of the
 * {@link edu.wustl.mir.erl.ihe.util.jdbc.Query#setAll(DBTable) Query.setAll}
 * method to plug all relevant values from a database class instance into a SQL
 * statement in one step.</li>
 * <li>Allows the use of the
 * {@link edu.wustl.mir.erl.ihe.util.jdbc.DBHelper#loadNextRow(java.sql.ResultSet)
 * DBHelper.loadNextRow()} and
 * {@link edu.wustl.mir.erl.ihe.util.jdbc.DBHelper#loadRows(java.sql.ResultSet)
 * loadRows()} methods to automated populating the properties of a database
 * class instance from a {@link java.sql.ResultSet ResultSet} row or an array of
 * instances from a ResultSet.</li>
 * </ul>
 * </li>
 * <li>The {@link edu.wustl.mir.erl.ihe.util.jdbc.JDBC JDBC} class encapsulates
 * the basic JDBC processes using a set of static methods which can:
 * <ul>
 * <li>Initialize JDBC databases using configuration information found in the
 * &lt;JDBC&gt; element in the application properties file.</li>
 * <li>Permits dynamic initialization and re-initialization of databases based
 * on parameters in a {@link java.util.Map Map}.</li>
 * <li>Serves as a factory for {@link java.sql.Connection Connections} along
 * with related {@link java.sql.Statement Statement} and
 * {@link java.sql.ResultSet ResultSet} wrapped in {@link java.io.Closeable
 * Closeable} {@link edu.wustl.mir.erl.ihe.util.jdbc.JDBC.Connection
 * JDBC.Connection} objects which can be instantiated using the Java 7 "try with
 * resources" syntax.</li>
 * <li>Handle both general purpose and simplified JDBC queries.</li>
 * </ul>
 * </li>
 * <li>The {@link edu.wustl.mir.erl.ihe.util.jdbc.Query Query} class, which
 * supports the use of parameterized SQL queries using ${paramName} syntax and
 * is integrated with {@link edu.wustl.mir.erl.ihe.util.jdbc.JDBC JDBC}.</li>
 * <li>The {@link edu.wustl.mir.erl.ihe.util.jdbc.Queries Queries} class, which
 * serves as a base class for classes encapsulating SQL query strings for a
 * particular RDBMS.</li>
 * <li>The {@link edu.wustl.mir.erl.ihe.util.jdbc.DBUtil DBUtil} interface,
 * which permits the use of plugable utility classes to perform maintenance
 * functions for different types of database servers. Currently, the only
 * implementation is {@link edu.wustl.mir.erl.ihe.util.jdbc.DBUtilPostgres
 * DBUtilPostgres}.</li>
 * </ul>
 * The jdbc package is designed to support java classes which model a table in
 * an RDBMS. Set up a table class by following these steps (The class WSLog in
 * the WS package is a good example to follow along with):
 * <ol>
 * <li>Create a java class for the table. Typically this is done in a sub
 * package ".db" of the application package.</li>
 * <li>Implement {@link java.io.Serializable Serializable} using the default
 * sequence.</li>
 * <li>Identify the logical database name (see
 * {@link edu.wustl.mir.erl.ihe.util.jdbc.JDBC#init(java.util.List, String)
 * here} for details) like this:
 * 
 * <pre>
 * public static final String logicalDbName = &quot;wslog&quot;;</pre>
 * </li>
 * <li>Define properties for each column of the table, using these guidelines:
 * <ul>
 * <li>Put all the property definitions together, in the same order as the
 * columns appear in the table.</li>
 * <li>The first property should be "id", an integer type which is the primary
 * unique key in the SQL table.</li>
 * <li>Java property names should be the camel case equivalent of the SQL table
 * column name. For example: Java "connOpenTime" corresponding to SQL
 * "conn_open_time". Choose SQL and Java names to avoid any ambiguity in
 * conversion between these two formats.</li>
 * <li>As much as possible, use standard SQL to maximize portability. We use <a
 * href="http://www.w3schools.com/sql">W3Schools SQL</a> as a guide rather than
 * getting into the ISO standard.</li>
 * <li>All properties should have public getters and setters. Use the
 * "isPropertyName" form for boolean getters.</li>
 * </ul>
 * </li>
 * <li>If the table is to appear in a GUI table with the option of selecting
 * one more more instances corresponding to rows of the GUI table, add the
 * following property, with standard getter and setter:
 * 
 * <pre>
 * private boolean selected = false;</pre>
 * 
 * <li>If you will want to sort arrays of instances of this class, add an inner
 * class implementing {@link java.util.Comparator Comparator}, utilizing
 * {@link edu.wustl.mir.erl.ihe.util.jdbc.DBHelper DBHelper} and the
 * {@link edu.wustl.mir.erl.ihe.util.jdbc.Helper Helper} annotation. See their
 * JavaDoc for details.</li>
 * <li>For each desired RDBMS type implement a subclass of the
 * {@link edu.wustl.mir.erl.ihe.util.jdbc.Queries Queries} class containing
 * query strings formatted for that RDBMS. See the JavaDoc for Queries for
 * details, and the WSLogPostgres class for a sample implementation.</li>
 * </ol>
 */
package edu.wustl.mir.erl.ihe.util.jdbc;

