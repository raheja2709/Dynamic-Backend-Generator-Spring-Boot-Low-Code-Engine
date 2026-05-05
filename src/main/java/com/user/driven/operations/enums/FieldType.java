package com.user.driven.operations.enums;

/**
 * Enumeration representing the type of a field in an entity. This helps define
 * how the field behaves in terms of database constraints and indexing.
 * 
 * <ul>
 * <li><b>PRIMARY_KEY</b>: The primary key field of the entity.</li>
 * <li><b>FOREIGN_KEY</b>: A field that references another entity’s primary
 * key.</li>
 * <li><b>NORMAL_FIELD</b>: A regular, unconstrained field.</li>
 * <li><b>UNIQUE_FIELD</b>: A field that must contain unique values across
 * records.</li>
 * <li><b>INDEX_FIELD</b>: A field that is indexed for faster querying.</li>
 * </ul>
 * 
 * This enum is typically used during entity generation and schema definition to
 * apply the correct annotations and constraints.
 * 
 * @author Jatin Raheja
 */
public enum FieldType {
	PRIMARY_KEY, FOREIGN_KEY, NORMAL_FIELD, UNIQUE_FIELD, INDEX_FIELD, NORMAL
}
