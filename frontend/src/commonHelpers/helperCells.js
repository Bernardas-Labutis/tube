import React, { Component } from "react";
import ImageCellView from "./imageCell";
import { Icon, Input, Popconfirm } from "antd";
import Checkbox from "../components/uielements/checkbox";

const DateCell = (data) => <p>{data.toLocaleString()}</p>;
const ImageCell = (src) => <ImageCellView src={src} />;
const LinkCell = (link, href) => <a href={href ? href : "#"}>{link}</a>;
const TextCell = (text) => <p>{text}</p>;

class EditableCell extends Component {
	constructor(props) {
		super(props);
		this.handleChange = this.handleChange.bind(this);
		this.check = this.check.bind(this);
		this.edit = this.edit.bind(this);
		this.state = {
			value: this.props.value,
			editable: false,
		};
	}
	handleChange(event) {
		const value = event.target.value;
		this.setState({ value });
	}
	check() {
		this.setState({ editable: false });
		if (this.props.onChange) {
			this.props.onChange(
				this.state.value,
				this.props.columnsKey,
				this.props.index
			);
		}
	}
	edit() {
		this.setState({ editable: true });
	}
	render() {
		const { value, editable } = this.state;
		const handleKeyDown = e => {
			if (e.key === "<"
				|| e.key === ">"
				|| e.key === ":"
				|| e.key === "\""
				|| e.key === "\\"
				|| e.key === "\/"
				|| e.key === "|"
				|| e.key === "?"
				|| e.key === "*") {
				e.preventDefault();
			}
		};
		return (
			<div onClick={(e) => e.stopPropagation()}>
				<div className="isoEditData">
					{editable ? (
						<div className="isoEditDataWrapper">
							<Input
								value={value}
								onChange={this.handleChange}
								onPressEnter={this.check}
								onKeyDown={handleKeyDown}
							/>
							<Icon
								type="check"
								className="isoEditIcon"
								onClick={this.check}
							/>
						</div>
					) : (
						<p className="isoDataWrapper">
							{value || " "}
							<Icon
								type="edit"
								className="isoEditIcon"
								onClick={this.edit}
							/>
						</p>
					)}
				</div>
			</div>
		);
	}
}

class VisibilityCell extends Component {
	render() {
		const { isPub, index, onVisibilityCell } = this.props;
		return (
			<div onClick={(e) => e.stopPropagation()}>
				<Popconfirm
					title="Sure to change visibility?"
					okText="YES"
					cancelText="No"
					onConfirm={() => onVisibilityCell(index, isPub)}
				>
					<Checkbox
						checked={isPub}
					/>
				</Popconfirm>
			</div>
		);
	}
}

class DeleteCell extends Component {
	render() {
		const { index, onDeleteCell } = this.props;
		return (
			<div onClick={(e) => e.stopPropagation()}>
				<Popconfirm
					title="Sure to delete?"
					okText="DELETE"
					cancelText="No"
					onConfirm={() => onDeleteCell(index)}
				>
					<a>Delete</a>
				</Popconfirm>
			</div>
		);
	}
}

class RestoreCell extends Component {
	render() {
		const { index, onRestoreCell } = this.props;
		return (
			<div onClick={(e) => e.stopPropagation()}>
				<Popconfirm
					title="Sure to restore?"
					okText="RESTORE"
					cancelText="No"
					onConfirm={() => onRestoreCell(index)}
				>
					<a>Restore</a>
				</Popconfirm>
			</div>
		);
	}
}

class DownloadCell extends Component {
	render() {
		const { index, onDownloadCell } = this.props;
		return (
			<div onClick={(e) => e.stopPropagation()}>
				<Popconfirm
					title="Sure to download?"
					okText="DOWNLOAD"
					cancelText="No"
					onConfirm={() => onDownloadCell(index)}
				>
					<a>Download</a>
				</Popconfirm>
			</div>
		);
	}
}

export {
	DateCell,
	ImageCell,
	LinkCell,
	TextCell,
	EditableCell,
	VisibilityCell,
	DeleteCell,
	RestoreCell,
	DownloadCell,
};
