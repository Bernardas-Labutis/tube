import React, { Component } from "react";
import clone from "clone";
import TableWrapper from "../../commonStyles/table.style";
import {
	DeleteCell,
	EditableCell,
	DownloadCell,
} from "../../commonHelpers/helperCells";
import { tableinfos } from "./configs";
import axios from "axios";
import dataMagic from "../../commonHelpers/dataMagic";
import "react-modal-video/css/modal-video.min.css";
import ModalVideo from "react-modal-video";
import PageHeader from "../../components/utility/pageHeader";
import LayoutWrapper from "../../components/utility/layoutWrapper";
import ContentHolder from "../../components/utility/contentHolder";
import Box from "../../components/utility/box";
import { Row, Col, Button } from "antd";
import basicStyle from "../../config/basicStyle";
import TubeShareCell from "./tubesharecell";
import Dropzone from "../../components/uielements/dropzone";
import DropzoneWrapper from "../../containers/AdvancedUI/dropzone/dropzone.style";
import { notification } from "../../components";
import FormData from "form-data";
import checkforHeader from '../../axiosheader'

export default class MyVideos extends Component {
	constructor(props) {
		super(props);
		this.onCellChange = this.onCellChange.bind(this);
		this.onDeleteCell = this.onDeleteCell.bind(this);
		this.onDownloadCell = this.onDownloadCell.bind(this);
		this.state = {
			columns: this.createcolumns(clone(tableinfos[0].columns)),
			dataList: [],
			isOpen: false,
			videoUrl: "",
			isUploadButtonEnabled: false,
			addedFile: null,
			dropzone: null,
		};
		this.openModal = this.openModal.bind(this);
	}
	openModal() {
		this.setState({ isOpen: true });
	}

	getData() {
		let data = [];
		checkforHeader();
		axios
			.get("/video/userAvailable", {})
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
		checkforHeader();
		axios
			.get("/video/viewingUrl/" + videoId)
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
				index={record.id}
				columnsKey={columns[0].key}
				value={text[columns[0].key]}
				onChange={this.onRename}
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
				<TubeShareCell index={record.id}/>
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
	onRename(value, columnsKey, index) {
		checkforHeader();
		axios
			.post(`/video/rename`, {id: index, newName: value});
			//.then(() => this.getData());
			// getData() is not a function somehow, no problem, cause UI shows rename immediately
	}
	onCellChange(value, columnsKey, index) {
		const { dataList } = [...this.state];
		dataList[index][columnsKey] = value;
		this.setState({ dataList });
	}
	onDeleteCell = (index) => {
		checkforHeader();
		axios
			.get(`/video/soft-delete/${index}`)
			.then(() => this.getData());
	};
	onDownloadCell = (index) => {
		checkforHeader();
		axios
			.get(`/video/download/${index}`)
			.then((response) => {
				const link = document.createElement("a");
				link.href = response.data.url;
				document.body.appendChild(link);
				link.click();
			});
	};
	upload(file) {
		console.log("added file");
		console.log(this.state.addedFile);
		const formData = new FormData();
		let fileName =
			file.name.substring(0, file.name.lastIndexOf(".")) || file.name;
		formData.append("fileName", fileName);
		formData.append("fileSize", 50);
		formData.append("file", file);
		checkforHeader();
		return axios
			.post("/video/upload", formData)
			.then((response) => {
				notification("success", `${file.name} successfully uploaded`),
					this.setState({
						isUploadButtonEnabled: false,
					}),
					console.log(response);
				this.removeFile();
			});
	}
	render() {
		const componentConfig = {
			method: true,
			showFiletypeIcon: true,
			uploadMultiple: false,
			maxFilesize: 1024, // 1gb
			maxFiles: 1,
			dictMaxFilesExceeded: "You can only upload 1 video at a time",
			dictRemoveFile: "Delete",
			dictCancelUploadConfirmation: "Are you sure to cancel upload?",
			postUrl: "no-url",
		};
		const djsConfig = {
			autoProcessQueue: false,
			acceptedFiles: "video/*",
			maxFiles: 1,
		};
		const eventHandlers = {
			addedfile: (file) => {
				notification("success", `${file.name} added`);
				this.setState({ isUploadButtonEnabled: true, addedFile: file });
				console.log(file);
			},
			success: (file) =>
				notification("success", `${file.name} successfully uploaded`),
			error: (error) => notification("error", "could not upload video"),
			init: this.initCallback.bind(this),
		};
		const { columns, dataList } = this.state;
		const { rowStyle, colStyle, gutter } = basicStyle;
		return (
			<LayoutWrapper>
				<PageHeader>My Videos</PageHeader>
				<Row style={rowStyle} gutter={gutter} justify="start">
					<Col md={24} sm={24} xs={24} style={colStyle}>
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
					</Col>
				</Row>
				<Row style={rowStyle} gutter={gutter}>
					<Col span={24}>
						<DropzoneWrapper>
							<Dropzone
								addedFile={this.state.addedFile}
								config={componentConfig}
								eventHandlers={eventHandlers}
								djsConfig={djsConfig}
								accept
							/>
						</DropzoneWrapper>
					</Col>
				</Row>
				<Row style={rowStyle} gutter={gutter}>
					<Col span={24}>
						<div style={{ float: "right" }}>
							<Button
								type="danger"
								disabled={!this.state.isUploadButtonEnabled}
								onClick={() => {
									this.removeFile();
									notification("info", "file removed");
								}}
							>
								Remove uploaded file
							</Button>
							<Button
								type="primary"
								disabled={!this.state.isUploadButtonEnabled}
								onClick={() => {
									this.uploadAndRefresh(this.state.addedFile);
								}}
							>
								Upload!
							</Button>
						</div>
					</Col>
				</Row>
			</LayoutWrapper>
		);
	}
	initCallback(dropzone) {
		console.log(dropzone);
		console.log("initCallback called");
		this.state.dropzone = dropzone;
		dropzone.on("addedfile", function (file) {
			if (this.files.length > 1) {
				this.removeFile(this.files[0]);
			}
		});
	}

	removeFile() {
		if (this.state.dropzone) {
			console.log(this.state.dropzone);
			console.log("removeFile called");
			this.state.dropzone.removeAllFiles();
		}
	}

	async uploadAndRefresh(file) {
		await this.upload(file);
		this.getData();
	}
}
