import React, { Component } from 'react';
import clone from 'clone';
import TableWrapper from '../../commonStyles/table.style';
import { EditableCell, DeleteCell, RestoreCell } from '../../commonHelpers/helperCells';
import {tableinfos} from "./configs";
import fakeData from "../../mock/fakeData";

const dataList = new fakeData(3);

export default class Trashcan extends Component {
    constructor(props) {
        super(props);
        this.onCellChange = this.onCellChange.bind(this);
        this.onDeleteCell = this.onDeleteCell.bind(this);
        this.state = {
            columns: this.createcolumns(clone(tableinfos[0].columns)),
            dataList: dataList.getAll(),
        };
    }
    createcolumns(columns) {
        const editColumnRender = (text, record, index) =>
            <EditableCell
                index={index}
                columnsKey={columns[0].key}
                value={text[columns[0].key]}
                onChange={this.onCellChange}
            />;
        columns[0].render = editColumnRender;
        const deleteColumn = {
            title: '',
            dataIndex: 'delete',
            render: (text, record, index) =>
                <DeleteCell index={index} onDeleteCell={this.onDeleteCell} />,
        };
        const restoreColumn = {
            title: 'Actions',
            dataIndex: 'restore',
            render: (text, record, index) =>
                <RestoreCell index={index} onDeleteCell={this.onRestoreCell} />,
        };
        columns.push(restoreColumn);
        columns.push(deleteColumn);
        return columns;
    }
    onCellChange(value, columnsKey, index) {
        const { dataList } = this.state;
        dataList[index][columnsKey] = value;
        this.setState({ dataList });
    }
    onDeleteCell = index => {
        const { dataList } = this.state;
        dataList.splice(index, 1);
        this.setState({ dataList });
    };
    onRestoreCell = index => {
        const { dataList } = this.state;
        dataList.splice(index, 1);
        this.setState({ dataList });
    };
    render() {
        const { columns, dataList } = this.state;

        return (
            <TableWrapper
                columns={columns}
                dataSource={dataList}
                className="isoEditableTable"
            />
        );
    }
}
