import * as cdk from 'aws-cdk-lib';
import { Construct } from 'constructs';
import {Iam} from "./iam";
import {GameLiftLambda} from "./lambda";
import {ApiGateway} from "./apigateway";
// import * as sqs from 'aws-cdk-lib/aws-sqs';

export class BackendServiceStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);
    const iamRole = new Iam(this,"lambdaRole");
    const gameLiftLambda = new GameLiftLambda(this, "gamelift-lambda", {
      lambdaRole: iamRole.lambdaRole
    })
    const  apigateway = new ApiGateway(this, "gamelift-APIGateway", {startGameSessionLambda:
      gameLiftLambda.startGameSessionFunction})

  }
}
