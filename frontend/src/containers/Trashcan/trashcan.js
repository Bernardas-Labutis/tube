import React, { Component } from "react";
import clone from "clone";
import TableWrapper from "../../commonStyles/table.style";
import {
	DeleteCell,
	EditableCell,
	RestoreCell,
} from "../../commonHelpers/helperCells";
import { tableinfos } from "./configs";
import axios from "axios";
import dataMagic from "../../commonHelpers/dataMagic";
import "react-modal-video/css/modal-video.min.css";
import ModalVideo from "react-modal-video";

(function() {
    var token = "something";
    var token = localStorage.id_token;
    var len = localStorage.id_token;
    console.log(len);
    if (token) {
        axios.defaults.headers.common['Authorization'] = token;
        console.log("1" + token);
    } else {
        axios.defaults.headers.common['Authorization'] = null;
        console.log("2" + token);
        /*if setting null does not remove `Authorization` header then try     
          delete axios.defaults.headers.common['Authorization'];
        */
    }
  })();

export default class Trashcan extends Component {
	constructor(props) {
		super(props);
		this.onCellChange = this.onCellChange.bind(this);
		this.onDeleteCell = this.onDeleteCell.bind(this);
		this.state = {
			columns: this.createcolumns(clone(tableinfos[0].columns)),
			dataList: [],
			isOpen: false,
			videoUrl: "",
		};
		this.openModal = this.openModal.bind(this);
	}
	openModal() {
		this.setState({ isOpen: true });
	}

	getData() {
		let data = [];
		axios
			.get("http://localhost:8080/video/soft-deleted", {})
			.then((response) => {
				console.log(response);
				data = response.data;
				console.log(data);
				if (data.length === 0) {
					this.setState({ dataList: [] });
				} else {
					this.setState({
						dataList: new dataMagic(data.length, data).getAll(),
					});
				}
			});
	}

	getVideoUrl(videoId) {
		let videoUrl = "";
		axios
			.get("http://localhost:8080/video/viewingUrl/" + videoId)
			.then((response) => {
				console.log(response);
				this.setState({
					videoUrl: response.data,
				});
			});
	}

	componentDidMount() {
		this.getData();
	}

	createcolumns(columns) {
		columns[0].render = (text, record, index) => (
			<EditableCell
				index={index}
				columnsKey={columns[0].key}
				value={text[columns[0].key]}
				onChange={this.onCellChange}
			/>
		);
		const deleteColumn = {
			title: "",
			dataIndex: "delete",
			render: (text, record, index) => (
				<DeleteCell
					index={record.id}
					onDeleteCell={this.onDeleteCell}
				/>
			),
		};
		const restoreColumn = {
			title: "Actions",
			dataIndex: "restore",
			render: (text, record, index) => (
				<RestoreCell
					index={record.id}
					onRestoreCell={this.onRestoreCell}
				/>
			),
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
	onDeleteCell = (index) => {
		axios
			.delete(`http://localhost:8080/video/${index}`)
			.then(() => this.getData());
	};
	onRestoreCell = (index) => {
		axios
			.get(`http://localhost:8080/video/recover/${index}`)
			.then(() => this.getData());
	};
	render() {
		const { columns, dataList } = this.state;

		return (
			<div>
				<h2>Trashcan</h2>
				<React.Fragment>
					<TableWrapper
						columns={columns}
						dataSource={dataList}
						/*onRowClick={(video) => {
							this.getVideoUrl(video.id);
							this.openModal();
						}}*/
						className="isoEditableTable"
					/>
					<ModalVideo
						channel="custom"
						isOpen={this.state.isOpen}
						url={this.state.videoUrl}
						onClose={() => this.setState({ isOpen: false })}
					/>
				</React.Fragment>
			</div>
		);
	}
}
