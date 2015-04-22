package io.github.avatarhurden.lifeorganizer.views;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.util.Callback;

public class CustomizableTableView<T> extends TableView<T> {

	private Map<String, TableColumn<T, ?>> columnList;
	private ContextMenu menu;

	public CustomizableTableView() {
		columnList = new HashMap<String, TableColumn<T, ?>>();
		menu = new ContextMenu();
		setTableMenuButtonVisible(true);
		
		setOnKeyPressed(event -> {
			if (event.getCode().equals(KeyCode.DELETE))
				getItems().remove(getSelectionModel().getSelectedItem());
		});
	}
	
	public <S> void addColumn(String name) {
		addColumn(name, null, null, null);
	}
	
	public <S> void addColumn(String name, Callback<T, ObservableValue<S>> property) {
		addColumn(name, property, null, null);
	}
	
	public <S> void addColumn(String name, Callback<T, ObservableValue<S>> property, Comparator<S> comparator) {
		addColumn(name, property, comparator, null);
	}
	
	public <S> void addColumn(String name, Callback<T, ObservableValue<S>> property, 
			Comparator<S> comparator, Callback<TableColumn<T, S>, TableCell<T, S>> cellFactory) {
		
		TableColumn<T, S> column = new TableColumn<T, S>(name);
		getColumns().add(column);
		columnList.put(name, column);
		
		if (property != null)
			column.setCellValueFactory(col -> property.call(col.getValue()));
		if (cellFactory != null)
			column.setCellFactory(cellFactory);
		if (comparator != null)
			column.setComparator(comparator);
		
		menu.getItems().add(createColumnMenuItem(column.getText(), column));
		column.setContextMenu(menu);
	}
	
	public <S> void addColumn(String name, TableColumn<T, S> column) {
		column.setText(name);
		getColumns().add(column);
		columnList.put(name, column);
		
		menu.getItems().add(createColumnMenuItem(column.getText(), column));
		column.setContextMenu(menu);
	}

	@SuppressWarnings("unchecked")
	public <S> void setCellFactory(String name, Callback<TableColumn<T, S>, TableCell<T, S>> call) {
		TableColumn<T, S> column = (TableColumn<T, S>) columnList.get(name);
		column.setCellFactory(call);
	}
	
	private MenuItem createColumnMenuItem(String name, TableColumn<T, ?> column) {
		CheckMenuItem item = new CheckMenuItem(name);
		item.selectedProperty().bindBidirectional(column.visibleProperty());
		
		return item;
	}

	@SuppressWarnings("rawtypes")
	public List<String> getColumnOrder() {
		List<String> list = new ArrayList<String>();
		for (TableColumn t : getColumns())
			list.add(t.getText());
		return list;
	}
	
	public void setColumnOrder(List<String> list) {
		getColumns().clear();
		for (String name : list)
			getColumns().add(columnList.get(name));
	}

	@SuppressWarnings("rawtypes")
	public List<Double> getColumnWidth() {
		List<Double> list = new ArrayList<Double>();
		for (TableColumn t : getColumns())
			list.add(t.getWidth());
		return list;
	}
	
	public void setColumnWidth(List<Double> list) {
		for (int i = 0; i < list.size(); i++)
			getColumns().get(i).setPrefWidth(list.get(i));
	}

	@SuppressWarnings("rawtypes")
	public List<Boolean> getColumnShown() {
		List<Boolean> list = new ArrayList<Boolean>();
		for (TableColumn t : getColumns())
			list.add(t.isVisible());
		return list;
	}
	
	public void setColumnShown(List<Boolean> list) {
		for (int i = 0; i < list.size(); i++)
			getColumns().get(i).setVisible(list.get(i));
	}

	@SuppressWarnings("rawtypes")
	public List<String> getColumnSortOrder() {
		List<String> list = new ArrayList<String>();
		for (TableColumn col : getSortOrder())
			list.add(col.getText()+"/"+col.getSortType());
		return list;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void setColumnSortOrder(List<String> list) {
		for (String s : list) {
			if (s.equals(""))
				continue;
			TableColumn col = columnList.get(s.split("/")[0]);
			col.setSortType(SortType.valueOf(s.split("/")[1]));
			getSortOrder().add(col);
		}
	}
	
}
