import React, { Component } from "react";
import { connect } from "react-redux";
import clone from "clone";
import { Link } from "react-router-dom";
import { Layout } from "antd";
import { Scrollbars } from "react-custom-scrollbars";
import Menu from "../../components/uielements/menu";
import SidebarWrapper from "./sidebar.style";

import appActions from "../../redux/app/actions";
import Logo from "../../components/utility/logo";
import { rtl } from "../../config/withDirection";
import axios from "axios";
import SingleProgressWidget from "../../../src/containers/Widgets/progress/progress-single";
import IsoWidgetsWrapper from "../../../src/containers/Widgets/widgets-wrapper";
import basicStyle from "../../config/basicStyle";
import checkforHeader from '../../axiosheader';
const { Sider } = Layout;

const {
	toggleOpenDrawer,
	changeOpenKeys,
	changeCurrent,
	toggleCollapsed,
} = appActions;
const stripTrailingSlash = (str) => {
	if (str.substr(-1) === "/") {
		return str.substr(0, str.length - 1);
	}
	return str;
};
class Sidebar extends Component {
	constructor(props) {
		super(props);
		this.handleClick = this.handleClick.bind(this);
		this.onOpenChange = this.onOpenChange.bind(this);
		this.state = {
			usedStorage: 0,
			maxStorage: 0,
		};
		this.getStorageData = this.getStorageData.bind(this);
	}

	componentDidMount() {
		this.getStorageData();
		setInterval(this.getStorageData, 10000);
	}

	getStorageData() {
		checkforHeader();
		axios
			.get("/video/userStorage")
			.then((response) => {
				this.setState({
					usedStorage: response.data.usedStorageBytes,
					maxStorage: response.data.maxStorageBytes,
				});
			});
	}

	handleClick(e) {
		this.props.changeCurrent([e.key]);
		if (this.props.app.view === "MobileView") {
			setTimeout(() => {
				this.props.toggleCollapsed();
				this.props.toggleOpenDrawer();
			}, 100);
		}
	}

	getStoragePercentage() {
		return Math.round(
			(this.state.usedStorage * 100) / this.state.maxStorage
		);
	}

	onOpenChange(newOpenKeys) {
		const { app, changeOpenKeys } = this.props;
		const latestOpenKey = newOpenKeys.find(
			(key) => !(app.openKeys.indexOf(key) > -1)
		);
		const latestCloseKey = app.openKeys.find(
			(key) => !(newOpenKeys.indexOf(key) > -1)
		);
		let nextOpenKeys = [];
		if (latestOpenKey) {
			nextOpenKeys = this.getAncestorKeys(latestOpenKey).concat(
				latestOpenKey
			);
		}
		if (latestCloseKey) {
			nextOpenKeys = this.getAncestorKeys(latestCloseKey);
		}
		changeOpenKeys(nextOpenKeys);
	}
	getAncestorKeys = (key) => {
		const map = {
			sub3: ["sub2"],
		};
		return map[key] || [];
	};

	renderView({ style, ...props }) {
		const viewStyle = {
			marginRight: rtl === "rtl" ? "0" : "-17px",
			paddingRight: rtl === "rtl" ? "0" : "9px",
			marginLeft: rtl === "rtl" ? "-17px" : "0",
			paddingLeft: rtl === "rtl" ? "9px" : "0",
		};
		return (
			<div
				className="box"
				style={{ ...style, ...viewStyle }}
				{...props}
			/>
		);
	}

	render() {
		// const { url, app, toggleOpenDrawer, bgcolor } = this.props;
		const { app, toggleOpenDrawer, customizedTheme } = this.props;
		const url = stripTrailingSlash(this.props.url);
		const collapsed = clone(app.collapsed) && !clone(app.openDrawer);
		const { openDrawer } = app;
		const mode = collapsed === true ? "vertical" : "inline";
		const prettyBytes = require("pretty-bytes");
		const { rowStyle, colStyle } = basicStyle;
		const onMouseEnter = (event) => {
			if (openDrawer === false) {
				toggleOpenDrawer();
			}
			return;
		};
		const onMouseLeave = () => {
			if (openDrawer === true) {
				toggleOpenDrawer();
			}
			return;
		};
		const scrollheight = app.height;
		const styling = {
			backgroundColor: customizedTheme.backgroundColor,
		};
		const submenuStyle = {
			backgroundColor: "rgba(0,0,0,0.3)",
			color: customizedTheme.textColor,
		};
		const submenuColor = {
			color: customizedTheme.textColor,
		};

		return (
			<SidebarWrapper>
				<Sider
					trigger={null}
					collapsible={true}
					collapsed={collapsed}
					width="240"
					className="isomorphicSidebar"
					onMouseEnter={onMouseEnter}
					onMouseLeave={onMouseLeave}
					style={styling}
				>
					<Logo collapsed={collapsed} />
					<Scrollbars
						renderView={this.renderView}
						style={{ height: scrollheight - 70 }}
					>
						<Menu
							onClick={this.handleClick}
							theme="dark"
							mode={mode}
							openKeys={collapsed ? [] : app.openKeys}
							selectedKeys={app.current}
							onOpenChange={this.onOpenChange}
							className="isoDashboardMenu"
						>
							<Menu.Item key={"my-videos"}>
								<Link to={`${url}/my-videos`}>
									<span
										className="isoMenuHolder"
										style={submenuColor}
									>
										<i className="ion-videocamera" />
										<span className="nav-text">
											My videos
										</span>
									</span>
								</Link>
							</Menu.Item>
							<Menu.Item key={"public-videos"}>
								<Link to={`${url}/public-videos`}>
									<span
										className="isoMenuHolder"
										style={submenuColor}
									>
										<i className="ion-social-youtube" />
										<span className="nav-text">
											Public videos
										</span>
									</span>
								</Link>
							</Menu.Item>
							<Menu.Item key={"trashcan"}>
								<Link to={`${url}/trashcan`}>
									<span
										className="isoMenuHolder"
										style={submenuColor}
									>
										<i className="ion-ios-trash" />
										<span className="nav-text">
											Trashcan
										</span>
									</span>
								</Link>
							</Menu.Item>
						</Menu>
						<IsoWidgetsWrapper>
							<SingleProgressWidget
								label={
									prettyBytes(
										Number(this.state.usedStorage)
									) +
									"/" +
									prettyBytes(Number(this.state.maxStorage))
								}
								percent={this.getStoragePercentage()}
								status="active"
								info={true}
							/>
						</IsoWidgetsWrapper>
					</Scrollbars>
				</Sider>
			</SidebarWrapper>
		);
	}
}

export default connect(
	(state) => ({
		app: state.App.toJS(),
		customizedTheme: state.ThemeSwitcher.toJS().sidebarTheme,
	}),
	{ toggleOpenDrawer, changeOpenKeys, changeCurrent, toggleCollapsed }
)(Sidebar);
