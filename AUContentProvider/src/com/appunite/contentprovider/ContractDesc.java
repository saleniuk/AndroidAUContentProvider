/*
 * Copyright (C) 2012 Jacek Marchwicki <jacek.marchwicki@gmail.com>
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
 * limitations under the License
 */

package com.appunite.contentprovider;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.provider.BaseColumns;

public class ContractDesc {
	
	private final String mTableName;
	private final String mIdField;
	protected final List<TableFieldDesc> mTableFieldDescs = new ArrayList<ContractDesc.TableFieldDesc>();
	
	public enum FieldType {
		TEXT("TEXT"), INTEGER("INTEGER"), REAL("REAL");

		private String mTypeString;

		FieldType(String typeString) {
			mTypeString = typeString;
		}

		public String getTypeString() {
			return mTypeString;
		}
	}
	
	private static class TableFieldDesc {

		public final String fieldName;
		public final FieldType fieldType;
		private boolean notNull;

		public TableFieldDesc(String fieldName, FieldType fieldType) {
			this.fieldName = fieldName;
			this.fieldType = fieldType;
			this.notNull = false;
		}
		
		public TableFieldDesc(String fieldName, FieldType fieldType, boolean notNull) {
			this.fieldName = fieldName;
			this.fieldType = fieldType;
			this.notNull = notNull;
		}
		
	}

	protected ContractDesc(String tableName, String idField) {
		this.mTableName = tableName;
		this.mIdField = idField;
	}
	
	public static class Builder {
		ContractDesc contractDesc;
		
		public Builder(String tableName, String idField) {
			contractDesc = new ContractDesc(tableName, idField);
		}
		
		public Builder addTableField(String fieldName, FieldType fieldType) {
			contractDesc.mTableFieldDescs.add(new TableFieldDesc(fieldName, fieldType));
			return this;
		}
		
		public Builder addTableNotNullField(String fieldName, FieldType fieldType) {
			contractDesc.mTableFieldDescs.add(new TableFieldDesc(fieldName, fieldType, true));
			return this;
		}
		
		public ContractDesc build() {
			return contractDesc;
		}
	}
	
	public ProjectionMap buildProjectionMap() {
		ProjectionMap.Builder builder = new ProjectionMap.Builder().add(BaseColumns._ID,
				mIdField).add(mIdField);
		Iterator<TableFieldDesc> fieldsIter = mTableFieldDescs.iterator();
		while (fieldsIter.hasNext()) {
			TableFieldDesc field = fieldsIter.next();
			builder.add(field.fieldName);
		}
		return builder.build();
	}
	
	public String sqlDropTable() {
		return String.format("DROP TABLE IF EXISTS %s", mTableName);
	}
	
	public String sqlCreateTable() {
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE ");
		sb.append(mTableName);
		sb.append(" (");
		sb.append(String.format("%s %s primary key autoincrement, ", mIdField,
				FieldType.INTEGER.getTypeString()));
		
		Iterator<TableFieldDesc> fieldsIter = mTableFieldDescs.iterator();
		while (fieldsIter.hasNext()) {
			TableFieldDesc field = fieldsIter.next();
			sb.append(String.format("%s %s ", field.fieldName, field.fieldType.getTypeString()));
			if (field.notNull) {
				sb.append("NOT NULL ");
			}
			if (fieldsIter.hasNext())
				sb.append(", ");
		}
		sb.append(" );");
		return sb.toString();
	}
}
