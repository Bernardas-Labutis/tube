
import React, { Component } from "react";
import axios from "axios";
import { Icon, Input, Popconfirm } from "antd";

export default class TubeShareCell extends Component {
	state = {
		url: "",
		shareId: "",
	};
	render() {
		const { index } = this.props;
		return (
			<div onClick={(e) => e.stopPropagation()}>
				<Popconfirm
					//Probably should add a loading spinner here or something
					title={
						<div>
							<p>Your share link: </p>
							<a href={this.state.url}>{this.state.url}</a>
						</div>
					}
					okText="Copy link"
					cancelText="Remove link"
					onConfirm={() => {navigator.clipboard.writeText(this.state.url)}}
					onCancel={() => {
						axios
							.delete(`/video/share/${this.state.shareId}`)
							.then((response) => {
								this.setState({"url": "", "shareId": ""});
						});
					}}
					icon={<span></span>}
				>
					<a onClick={(e)=> {
						axios
							.get(`/video/share/${index}`)
							.then((response) => {
								this.setState({
									"url": `${window.location.protocol}//${window.location.host}/share/${response.data}`,
									"shareId": response.data
								});
							});
					}}>Share</a>
				</Popconfirm>
			</div>
		);
	}
}