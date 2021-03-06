import React, { Component } from "react";
import PageHeader from "../../components/utility/pageHeader";
import { siteConfig } from '../../config.js';
import axios from "axios";
import './sharedVideo.css';
import '../App/global.css';
export default class SharedVideo extends Component {
	constructor(props) {
		super(props);
		this.state = {
			uuid: this.props.match.params.uuid,
			videoData: {
				id: "",
				key: "",
				title: "",
				uploadTime: "",
				size: 0,
				ownerUsername: "",
				viewUrl: "",
				downloadUrl: ""
			}
		};
	}
	getData() {
		return axios
		.get(`/video/share/get/${this.state.uuid}`)
		.catch((error) => {
			if (error.response) {
				if (error.response.status == 404) {
					this.props.history.push("/404");
				}
			}
		})
		.then((data) => this.setState({videoData: data.data}));
	}
	componentDidMount() {
		this.getData();
	}
	render() {
		return (
		<div >    
			<h3 className="header">
				{siteConfig.siteName}
			</h3>
			<div className="content">
				<video className="videoPlayer" width="940px" height="523px" controls autoPlay src={this.state.videoData.viewUrl}>
					Sorry, your browser doesn't support embedded videos.
				</video>
				<p>Title: <span>{this.state.videoData.title}</span></p>
				<p>Uploaded: <span>{this.state.videoData.uploadTime}</span></p>
				<p>Owner: <span>{this.state.videoData.ownerUsername}</span></p>
				<button 
					className="ant-btn"
					onClick={
						(e) => {
							e.preventDefault();
							window.location.href=this.state.videoData.downloadUrl;
						}
					}
					disabled={!this.state.videoData.downloadUrl}
				>Download video</button>
			</div>
		</div>
            
		);
	}
}
