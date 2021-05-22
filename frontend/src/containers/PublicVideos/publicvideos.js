import React, { Component } from "react";
import clone from "clone";
import TableWrapper from "../../commonStyles/table.style";
import { tableinfos } from "./configs";
import axios from "axios";
import dataMagic from "../../commonHelpers/dataMagic";
import "react-modal-video/css/modal-video.min.css";
import ModalVideo from "react-modal-video";
import PageHeader from "../../components/utility/pageHeader";
import LayoutWrapper from "../../components/utility/layoutWrapper";
import { Row, Col } from "antd";
import basicStyle from "../../config/basicStyle";
import checkforHeader from "../../axiosheader";

export default class PublicVideos extends Component {
	constructor(props) {
		super(props);
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
		checkforHeader();
		axios.get("/video/publicAvailable", {}).then((response) => {
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
		axios.get("/video/viewingUrl/" + videoId).then((response) => {
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
		return columns;
	}

	render() {
		const { columns, dataList } = this.state;
		const { rowStyle, colStyle, gutter } = basicStyle;
		return (
			<LayoutWrapper>
				<PageHeader>Public Videos</PageHeader>
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
			</LayoutWrapper>
		);
	}
}
