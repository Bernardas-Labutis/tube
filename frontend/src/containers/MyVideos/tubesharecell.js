
import React, { Component } from "react";
import axios from "axios";
import { Icon, Input, Popconfirm } from "antd";
import checkforHeader from '../../axiosheader';

export default class TubeShareCell extends Component {
	constructor(props) {
		super(props);
		this.state = {
			url: "",
			shareId: "",
			visible: false,
			deleted: false,
			deleting: false
		};
	}
	title() {
		//Probably should add a loading spinner here or something
		if(this.state.deleted) {
			return (
				<div>
					<p>Your share link has been removed</p>
				</div>
			);
		}
		else {
			return (
				<div>
					<p>Your share link: </p>
					<a href={this.state.url}>{this.state.url}</a>
				</div>
			);
		}
 	}
	okText() {
		if(this.state.deleted) {
			return "Close";
		}
		else {
			return "Copy Link";
		}
	}
 	render() {
		const { index } = this.props;
		return (
			<div onClick={(e) => e.stopPropagation()}>
				<Popconfirm
					title={this.title()}
					okText={this.okText()}
					cancelText="Remove Link"
					cancelButtonProps={{disabled: this.state.deleted || this.state.deleting || this.state.url == ""}}
					onConfirm={() => {
						if(!this.state.deleted) {
							navigator.clipboard.writeText(this.state.url)
						}
					}}
					onCancel={() => {
						this.setState({
							deleting: true
						});
						checkforHeader();
						axios
							.delete(`/video/share/${this.state.shareId}`)
							.then((response) => {
								this.setState({
									url: "",
									shareId: "",
									deleted: true
								});
						});
					}}
					visible={this.state.visible}
					icon={<span></span>}
					onVisibleChange={(visibility, e)=>{
						if(visibility == false) {
							if(e && e.target) {
								if(e.target.className.includes("ant-btn-primary")) {
									this.setState({visible: false});
								}
							}
							else{
								this.setState({visible: false});
							}

						}
					}}
				>
					<a onClick={(e)=> {
						this.setState({
							visible: true,
							deleted: false,
							deleting: false
						});
						checkforHeader();
						axios
							.get(`/video/share/${index}`)
							.then((response) => {
								this.setState({
									url: `${window.location.protocol}//${window.location.host}/share/${response.data}`,
									shareId: response.data
								});
							});
						}}
					>Share</a>
				</Popconfirm>
			</div>
		);
	}
}