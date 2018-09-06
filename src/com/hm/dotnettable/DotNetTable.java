package com.hm.dotnettable;

import com.hm.utilities.ExcelHelper;
import com.sun.javafx.scene.control.skin.NestedTableColumnHeader;
import com.sun.javafx.scene.control.skin.TableColumnHeader;
import com.sun.javafx.scene.control.skin.TableHeaderRow;
import com.sun.javafx.scene.control.skin.TableViewSkin;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class DotNetTable
{

	CellValueChangeListener cellValueChangeListener;
	int cellValueChangeListenerColIndex = -1;

	TableView<TableModel> table;

	public DotNetTable(TableView<TableModel> table, String[] columnNames, boolean checkbox)
	{
		this.table = table;
		this.table.setEditable(true);
		this.table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		tickCheckBoxOnSpacePress(table);
		createColumns(columnNames, checkbox);
	}

	private void createColumns(String[] columnNames, boolean checkbox)
	{
		addIdColumn(table);
		if (checkbox)
		{
			addCheckboxColumn(table);
		}
		for (int i = 0; i < columnNames.length; i++)
		{
			TableColumn<TableModel, String> tc = new TableColumn<>(columnNames[i]);
			tc.setSortable(false);
			table.getColumns().add(tc);
			setCellValueFactory(tc, i);
		}
	}

	public void setData(int[] ids, List<List<String>> data)
	{
		ObservableList<TableModel> tableModels = table.getItems();
		if (tableModels == null)
		{
			tableModels = FXCollections.observableArrayList();
		}

		tableModels.clear();

		for (int i = 0; i < data.size(); i++)
		{
			tableModels.add(new TableModel(ids[i], data.get(i)));
		}
		table.setItems(tableModels);

		if (cellValueChangeListener != null && cellValueChangeListenerColIndex != -1)
		{
			for (TableModel tableModel : tableModels)
			{
				tableModel.getData().get(cellValueChangeListenerColIndex).addListener(new ChangeListener<String>()
				{
					@Override
					public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
					{
						cellValueChangeListener.onChanged();
					}
				});
			}
		}

		setAutoResizeColumnBasidOnContent(table);
	}

	public void addRows(int[] ids, List<List<String>> data)
	{
		ObservableList<TableModel> tableModels = table.getItems();
		if (tableModels == null)
		{
			tableModels = FXCollections.observableArrayList();
		}

		for (int i = 0; i < data.size(); i++)
		{
			tableModels.add(new TableModel(ids[i], data.get(i)));
		}

		if (cellValueChangeListener != null && cellValueChangeListenerColIndex != -1)
		{
			for (TableModel tableModel : tableModels)
			{
				tableModel.getData().get(cellValueChangeListenerColIndex).addListener(new ChangeListener<String>()
				{
					@Override
					public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
					{
						cellValueChangeListener.onChanged();
					}
				});
			}
		}

		setAutoResizeColumnBasidOnContent(table);
	}

	public void addRow(int id, List<String> data)
	{
		ObservableList<TableModel> tableModels = table.getItems();
		tableModels.add(new TableModel(id, data));

		setAutoResizeColumnBasidOnContent(table);
	}

	public void setData(List<List<String>> data)
	{
		ObservableList<TableModel> tableModels = table.getItems();
		if (tableModels == null)
		{
			tableModels = FXCollections.observableArrayList();
		}

		tableModels.clear();

		for (int i = 0; i < data.size(); i++)
		{
			tableModels.add(new TableModel(data.get(i)));
		}
		table.setItems(tableModels);
		setAutoResizeColumnBasidOnContent(table);
	}

	public int getSelectedId()
	{
		int selectedIndex = table.getSelectionModel().getSelectedIndex();
		return Integer.parseInt(table.getItems().get(selectedIndex).getId().getValue());
	}

	public void tickAll()
	{
		for (TableModel tm : table.getItems())
		{
			tm.getChecked().set(true);
		}
	}

	public void tickInverse()
	{
		for (TableModel tm : table.getItems())
		{
			tm.getChecked().set(!tm.getChecked().getValue());
		}
	}

	public void untickAll()
	{
		for (TableModel tm : table.getItems())
		{
			tm.getChecked().set(false);
		}
	}

	public void tick(int rowIndex)
	{
		table.getItems().get(rowIndex).getChecked().set(true);
	}

	public void untick(int rowIndex)
	{
		table.getItems().get(rowIndex).getChecked().set(false);
	}

	public boolean isTicked(int rowIndex)
	{
		return table.getItems().get(rowIndex).getChecked().getValue();
	}

	public List<Integer> getCheckedIndices()
	{
		ObservableList<TableModel> list = table.getItems();
		List<Integer> checkedIndices = new ArrayList<>();
		for (int i = 0; i < list.size(); i++)
		{
			TableModel tableModel = list.get(i);
			if (tableModel.getChecked().getValue())
			{
				checkedIndices.add(i);
			}
		}
		return checkedIndices;
	}

	public List<Integer> getSelectedIndices()
	{
		ObservableList<Integer> list = table.getSelectionModel().getSelectedIndices();
		List<Integer> selectedIndices = new ArrayList<>();
		for (int i = 0; i < list.size(); i++)
		{
			selectedIndices.add(list.get(i).intValue());
		}
		return selectedIndices;
	}

	private static void addCheckboxColumn(TableView<TableModel> tableView)
	{
		TableColumn<TableModel, Boolean> tc = new TableColumn<>("");
		tc.setSortable(false);
		tc.setEditable(true);
		tableView.getColumns().add(tc);
		setCheckBoxCellFactory(tableView, tc);
	}

	private static void addIdColumn(TableView<TableModel> tableView)
	{
		TableColumn<TableModel, String> tc = new TableColumn<>();
		tc.setSortable(false);
		tc.setVisible(false);
		tableView.getColumns().add(tc);
		setIdCellValueFactory(tc);
	}

	private static void setCheckBoxCellFactory(TableView<TableModel> tableView, TableColumn<TableModel, Boolean> tctest)
	{

		tctest.setCellFactory(new Callback<TableColumn<TableModel, Boolean>, TableCell<TableModel, Boolean>>()
		{
			@Override
			public TableCell<TableModel, Boolean> call(TableColumn<TableModel, Boolean> param)
			{
				CheckBoxTableCell cbtc = new CheckBoxTableCell<>();
				cbtc.setFocusTraversable(false);
				return cbtc;
			}
		});
		setCheckBoxCellValueFactory(tctest);
	}

	private static void tickCheckBoxOnSpacePress(TableView<TableModel> tableView)
	{

		tableView.setOnKeyPressed(new EventHandler<KeyEvent>()
		{
			@Override
			public void handle(KeyEvent event)
			{
				if (event.getCode() == KeyCode.SPACE && tableView.getSelectionModel().getSelectedItems().size() > 0)
				{
					for (TableModel tm : tableView.getSelectionModel().getSelectedItems())
					{
						tm.getChecked().set(!tm.getChecked().getValue());

					}

				}
			}
		});
	}

	private static void setIdCellValueFactory(TableColumn<TableModel, String> tctest)
	{

		tctest.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<TableModel, String>, ObservableValue<String>>()
		{
			@Override
			public ObservableValue<String> call(TableColumn.CellDataFeatures<TableModel, String> param)
			{
				return (param.getValue().getId());
			}
		});
	}

	private static void setCheckBoxCellValueFactory(TableColumn<TableModel, Boolean> tctest)
	{

		tctest.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<TableModel, Boolean>, ObservableValue<Boolean>>()
		{
			@Override
			public ObservableValue<Boolean> call(TableColumn.CellDataFeatures<TableModel, Boolean> param)
			{
				return param.getValue().getChecked();
			}
		});
	}

	private static void setCellValueFactory(TableColumn<TableModel, String> tctest, int valueIndex)
	{

		tctest.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<TableModel, String>, ObservableValue<String>>()
		{
			@Override
			public ObservableValue<String> call(TableColumn.CellDataFeatures<TableModel, String> param)
			{
				return (param.getValue().getData().get(valueIndex));
			}
		});
	}

	private static void setAutoResizeColumnBasidOnContent(TableView table)
	{
		TableViewSkin<?> skin = (TableViewSkin<?>) table.getSkin();

		if (skin == null)
		{
			return;
		}

		TableHeaderRow headerRow = skin.getTableHeaderRow();
		NestedTableColumnHeader rootHeader = headerRow.getRootHeader();
		for (TableColumnHeader columnHeader : rootHeader.getColumnHeaders())
		{
			try
			{
				TableColumn<?, ?> column = (TableColumn<?, ?>) columnHeader.getTableColumn();
				if (column != null)
				{
					Method method = skin.getClass().getDeclaredMethod("resizeColumnToFitContent", TableColumn.class, int.class);
					method.setAccessible(true);
					method.invoke(skin, column, 30);
				}
			}
			catch (Throwable e)
			{
				e = e.getCause();
				e.printStackTrace(System.err);
			}
		}
	}

	public void makeColumnEditable(int colIndex, CellValueChangeListener cellValueChangeListener)
	{
		TableColumn<TableModel, String> tc = (TableColumn<TableModel, String>) table.getColumns().get(colIndex + 1);
		tc.setEditable(true);
		tc.setPrefWidth(100);
		tc.setCellFactory(TextFieldTableCell.forTableColumn());
		setCellValueFactory(tc, colIndex);

		this.cellValueChangeListener = cellValueChangeListener;
		this.cellValueChangeListenerColIndex = colIndex;
	}

	public Object getValueAt(int rowIndex, int colIndex)
	{
		return table.getItems().get(rowIndex).getData().get(colIndex).getValue();
	}

	public int getIdAt(int i)
	{
		return Integer.parseInt(table.getItems().get(i).getId().getValue());
	}

	public void selectRowById(int id)
	{

		for (TableModel tm : table.getItems())
		{
			if (tm.getId().getValue().equals(String.valueOf(id)))
			{
				table.getSelectionModel().select(tm);
				table.scrollTo(tm);
				return;
			}
		}
	}

	public void tickSelected()
	{
		for (Object i : table.getSelectionModel().getSelectedIndices())
		{
			tick((int) i);
		}
	}

	public void untickSelected()
	{
		for (Object i : table.getSelectionModel().getSelectedIndices())
		{
			untick((int) i);
		}
	}

	public void createExcel(String path) throws FileNotFoundException, java.io.IOException
	{

		XSSFWorkbook wb = new XSSFWorkbook(); //Excell workbook
		XSSFSheet sheet = wb.createSheet(); //WorkSheet
		XSSFRow sheetRow; //Row created at line 3
		//TableModel model = table.getModel(); //Table model

		sheetRow = ExcelHelper.createRow(sheet); //Create row at line 0
		for (int headings = 0; headings < table.getColumns().size(); headings++)
		{ //For each column

			ExcelHelper.createCell(sheetRow).setCellValue(table.getColumns().get(headings).getText());//Write column name
		}

		int rowCount = table.getItems().size();

		for (int rowIndex = 0; rowIndex < rowCount; rowIndex++)
		{ //For each table row
			sheetRow = ExcelHelper.createRow(sheet);

			TableModel tm = table.getItems().get(rowIndex);
			int colCount = tm.getData().size() + 1;

			for (int colIndex = 0; colIndex < colCount; colIndex++)
			{
				//For each table column

				String value = colIndex == 0 ? tm.getId().getValue() : tm.getData().get(colIndex - 1).getValue();

				ExcelHelper.createCell(sheetRow).setCellValue(value); //Write value
			}

		}
		wb.write(new FileOutputStream(path));//Save the file     
	}

}
