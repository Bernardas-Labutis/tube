import React, {Component} from 'react';
import clone from 'clone';
import TableWrapper from '../../commonStyles/table.style';
import {DeleteCell, EditableCell, RestoreCell} from '../../commonHelpers/helperCells';
import {tableinfos} from "./configs";
import axios from 'axios';
import dataMagic from "./dataMagic";

export default class Trashcan extends Component {
    constructor(props) {
        super(props);
        this.onCellChange = this.onCellChange.bind(this);
        this.onDeleteCell = this.onDeleteCell.bind(this);
        this.state = {
            columns: this.createcolumns(clone(tableinfos[0].columns)),
            dataList: [],
        };
    }

    getData() {
        let data = []
        axios.get('http://localhost:8080/video/soft-deleted', {})
            .then(response => {
                console.log(response);
                data = response.data;
                console.log(data);
                if (data.length === 0) {
                    this.setState({dataList: []})
                } else {
                    this.setState({dataList: new dataMagic(data.length, data).getAll()});
                }
            })
    }

    componentDidMount() {
        this.getData();
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
                <DeleteCell index={record.id} onDeleteCell={this.onDeleteCell} />,
        };
        const restoreColumn = {
            title: 'Actions',
            dataIndex: 'restore',
            render: (text, record, index) =>
                <RestoreCell index={record.id} onRestoreCell={this.onRestoreCell} />,
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
        axios.delete(`http://localhost:8080/video/${index}`)
            .then(() => this.getData())
    };
    onRestoreCell = index => {
        axios.get(`http://localhost:8080/video/recover/${index}`)
            .then(() => this.getData())
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
