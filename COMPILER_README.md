#ADHD-Compiler
---

##Getting Started
In order to use ADHD-Compiler you have to do the following...

####Gradle
[`android-apt`](https://bitbucket.org/hvisser/android-apt) (optional, prevents IDE from showing errors for code generated code)
	
######In build.gradle include

```
buildscript {
	...
	dependencies {
		...
		classpath 'com.neenbedankt.gradle.plugins:android-apt:1.4'
	}
}
		
apply plugin: 'com.neenbedankt.android-apt'
		
dependencies {
	...
	compile 'com.vokalinteractive:adhd-compiler:1.1.5'
    compile 'com.vokalinteractive:adhd:1.1.5@aar'
}

```

####Models
To create a model...

- Extend `DataModel`
- Create empty default constructor

````java
public class ExampleModel extends DataModel {
	public ExampleModel() {}	
}
````

####Annotations
Types of annotations...

```
@Table(String[] uniqueColumns, Names[] indexColumns)
@Column(Constraint[] constraint, String defaultValue)
@Names(String[] value)
```
How to use

- `@Table` 
	- Needs to be placed `before the class` declaration and the class needs to be public
	- The `uniqueColumns` parameter is used to set multiple uniques constraints on a table. It takes an array of strings. Each String should correspond to the field name and types exactly the same (see example below)
	- The `indexColumns` parameter is used to set multiple indices constraints on a table. It takes an array of @Names (see @Names to see how to use).
- `@Column`
	- Needs to be placed `before the field` declaration that you would like to be a column and the field has to be public
	- The `constraint` parameter is used to set the constraints on the column. Current options are
		- none
		- notNull
		- unique
	- If the last constraint notNull is used you must specify the `defaultValue` parameter (see example below)
- `@Names`
	- Used to group arrays of columns/fields as strings
	- Each String should correspond to the field name and types exactly the same (see example below)

Example...

````java
@Table(uniqueColumns = {"mNumber", "mString"},
	   indexColumns = { @Names({"mNumber", "mString"}),
	    				@Names({"mString", "mNumber"})})
//OR
@Table(uniqueColumns = "mString", indexColumns = @Names("mNumber"))
public class ExampleModel extends DataModel {
	@Column(constraint = notNull, defaultValue = "42") 
	public int mNumber;
	@Column(constraint = unique) 			
	public String mString;
	
	public ExampleModel() {}	
}
````

##Advanced
The Generated code is not capable of doing everything. Here are some helpful hints for things that might be encoutered.


####TableCreator
- If there are some extra things that you would like to add to a table before it is created you can @Override `onTableCreate`. A good example of when this might be helpful is when you would like to seed the database table with content.
- Here is how to do that

	````java
	...
	@Override
    public void onTableCreate(SQLiteTable.Builder aBuilder) {
    	super.onTableCreate(aBuilder);    	
    	aBuilder.seed(<some contentValues>);
    }
    ...
	````

