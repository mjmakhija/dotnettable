package com.hm.dotnettable;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TableModel
{

	private StringProperty id = new SimpleStringProperty();
	private BooleanProperty checked = new SimpleBooleanProperty();
	//private Boolean checkedWOProperty ;
	private List<SimpleStringProperty> data = new ArrayList<>();
	//private List<String> dataString;

	public TableModel(List<String> dataString)
	{
		this(0, dataString);
	}

	public TableModel(int id, List<String> dataString)
	{
		this.id.set(String.valueOf(id));
		checked.set(false);

		for (String string : dataString)
		{
			data.add(new SimpleStringProperty(string));
		}
	}

	public StringProperty getId()
	{
		return id;
	}

	public BooleanProperty getChecked()
	{
		return checked;
	}

	public void setChecked(BooleanProperty checked)
	{
		this.checked = checked;
	}

	public List<SimpleStringProperty> getData()
	{
		return data;
	}

	public void setData(List<SimpleStringProperty> data)
	{
		this.data = data;
	}

}
