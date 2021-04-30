import React, {Component} from 'react';
import clone from 'clone';
import TableWrapper from '../../commonStyles/table.style';
import {DeleteCell, EditableCell, RestoreCell} from '../../commonHelpers/helperCells';
import {tableinfos} from "./configs";
import fakeData from "../../mock/fakeData";
import axios from 'axios';

const dataList = new fakeData(3);
async function getSoftDeleted() {
    axios.get('http://localhost:8080/video/soft-deleted', {withCredentials: false})
        .then(function(response) {
            console.log(response);
        })
}

export default class Trashcan extends Component {
    constructor(props) {
        super(props);
        this.onCellChange = this.onCellChange.bind(this);
        this.onDeleteCell = this.onDeleteCell.bind(this);
        this.state = {
            columns: this.createcolumns(clone(tableinfos[0].columns)),
            dataList: dataList.getAll(),
        };
        getSoftDeleted();
    }
    createcolumns(columns) {
        columns[0].render = (text, record, index) =>
            <EditableCell
                index={index}
                columnsKey={columns[0].key}
                value={text[columns[0].key]}
                onChange={this.onCellChange}
            />;
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
            <div>
                <h2>Trashcan</h2>
            <TableWrapper
                columns={columns}
                dataSource={dataList}
                className="isoEditableTable"
            /></div>
        );
    }
}
