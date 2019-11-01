/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hibernate.bugs;

import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.testing.junit4.BaseCoreFunctionalTestCase;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * This template demonstrates how to develop a test case for Hibernate ORM, using its built-in unit test framework.
 * Although ORMStandaloneTestCase is perfectly acceptable as a reproducer, usage of this class is much preferred.
 * Since we nearly always include a regression test with bug fixes, providing your reproducer using this method
 * simplifies the process.
 *
 * What's even better?  Fork hibernate-orm itself, add your test case directly to a module's unit tests, then
 * submit it as a PR!
 */
public class ORMUnitTestCase extends BaseCoreFunctionalTestCase {

	@Override
	protected Class<?>[] getAnnotatedClasses() {
		return new Class[] { Employee.class, Task.class };
	}

	@Test
	public void testIt() {
		inTransaction( session -> {
			Employee employee = new Employee( "emp" );
			Task task = new Task( "t1", employee );
			employee.setTask( task );

			session.persist( employee );
			session.persist( task );

		} );

		inTransaction(
				session -> {
					List<Employee> employees = session.createQuery( "from Employee", Employee.class ).list();
					assertThat( employees.size(), is( 1 ) );
				}
		);
	}

	@Entity(name = "Employee")
	public static class Employee {
		private String name;

		private Task task;

		public Employee() {
		}

		public Employee(String name) {
			setName( name );
		}

		@Id
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@OneToOne(mappedBy = "employee", optional = false)
		public Task getTask() {
			return task;
		}

		public void setTask(Task task) {
			this.task = task;
		}
	}

	@MappedSuperclass
	public static abstract class BaseEntity {

		private String id;

		protected Employee employee = null;

		protected BaseEntity() {
		}

		protected BaseEntity(Employee employee) {
			this.setId( employee.getName() );
			this.employee = employee;
		}

		@Id
		@Column(name = "id", insertable = true, updatable = false)
		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public void setEmployee(Employee employee) {
			this.employee = employee;
		}
	}

	@Entity(name = "Task")
	public static class Task extends BaseEntity {
		private String name;

		public Task() {
		}

		public Task(String name, Employee e) {
			super( e ); // association set in super
			setName( name );
		}

		public String getName() {
			return name;
		}

		@OneToOne
		@JoinColumn(name = "id", nullable = false)
		@MapsId
		public Employee getEmployee() {
			return employee;
		}

		public void setName(String name) {
			this.name = name;
		}
	}
}
