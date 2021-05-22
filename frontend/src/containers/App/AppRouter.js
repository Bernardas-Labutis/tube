import React from "react";
import { Switch, Route } from "react-router-dom";
import asyncComponent from "../../helpers/AsyncFunc";

class AppRouter extends React.Component {
	render() {
		const { url } = this.props;
		return (
			<Switch>
				<Route
					exact
					path={`${url}/my-videos`}
					component={asyncComponent(() =>
						import("../MyVideos/myvideos")
					)}
				/>
				{
					<Route
						exact
						path={`${url}/public-videos`}
						component={asyncComponent(() =>
							import("../PublicVideos/publicvideos")
						)}
					/>
				}
				<Route
					exact
					path={`${url}/trashcan`}
					component={asyncComponent(() =>
						import("../Trashcan/trashcan")
					)}
				/>
			</Switch>
		);
	}
}

export default AppRouter;
