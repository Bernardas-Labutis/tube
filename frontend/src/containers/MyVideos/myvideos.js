import React, { Component } from "react";
import clone from "clone";
import TableWrapper from "../../commonStyles/table.style";
import {
	DeleteCell,
	EditableCell,
	ShareCell,
	DownloadCell,
} from "../../commonHelpers/helperCells";
import { tableinfos } from "./configs";
import axios from "axios";
import dataMagic from "../../commonHelpers/dataMagic";
import "react-modal-video/css/modal-video.min.css";
import ModalVideo from "react-modal-video";

export default class MyVideos extends Component {
	constructor(props) {
		super(props);
		this.onCellChange = this.onCellChange.bind(this);
		this.onDeleteCell = this.onDeleteCell.bind(this);
		this.onShareCell = this.onShareCell.bind(this);
		this.onDownloadCell = this.onDownloadCell.bind(this);
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
			.get("http://localhost:8080/video/allVideos", {})
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
		columns[3].render = (text, record, index) => (
			<EditableCell
				index={index}
				columnsKey={columns[3].key}
				value={text[columns[3].key]}
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
		const shareColumn = {
			title: "Actions",
			dataIndex: "share",
			render: (text, record, index) => (
				<ShareCell index={record.id} onShareCell={this.onShareCell} />
			),
		};
		const downloadColumn = {
			title: "",
			dataIndex: "download",
			render: (text, record, index) => (
				<DownloadCell
					index={record.id}
					onDownloadCell={this.onDownloadCell}
				/>
			),
		};
		columns.push(deleteColumn);
		columns.push(shareColumn);
		columns.push(downloadColumn);
		return columns;
	}
	onCellChange(value, columnsKey, index) {
		const { dataList } = this.state;
		dataList[index][columnsKey] = value;
		this.setState({ dataList });
	}
	onDeleteCell = (index) => {
		axios
			.get(`http://localhost:8080/video/soft-delete/${index}`)
			.then(() => this.getData());
	};
	onShareCell = (index) => {
		/*axios
            .get(`http://localhost:8080/video/recover/${index}`)
            .then(() => this.getData());*/
	};
	onDownloadCell = (index) => {
		/*axios
            .get(`http://localhost:8080/video/recover/${index}`)
            .then(() => this.getData());*/
	};
	render() {
		const { columns, dataList } = this.state;

		return (
			<div>
				<h2>My videos</h2>
				<React.Fragment>
					<TableWrapper
						columns={columns}
						dataSource={dataList}
						onRowClick={(video) => {
							this.getVideoUrl(video.id);
							this.openModal();
						}}
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
