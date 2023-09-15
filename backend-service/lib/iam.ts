import {Construct} from "constructs";
import * as iam from 'aws-cdk-lib/aws-iam';
import * as cdk from 'aws-cdk-lib';
export class Iam extends Construct {
    public readonly lambdaRole: iam.Role
    constructor(scope: Construct, id: string) {
        super(scope, id);
        this.lambdaRole = new iam.Role(this, 'ApiGatewayLambdaRole', {
            roleName: `java-gamelift-LambdaRole-${cdk.Stack.of(this).region}`,
            assumedBy: new iam.ServicePrincipal('lambda.amazonaws.com'),
            managedPolicies: [
                iam.ManagedPolicy.fromAwsManagedPolicyName('CloudWatchFullAccess'),
            ],
            inlinePolicies: {
                'GameLiftFullAccess': new iam.PolicyDocument({
                    assignSids: true,
                    statements: [
                        new iam.PolicyStatement({
                            effect: iam.Effect.ALLOW,
                            actions: [
                                'gamelift:*'
                            ],
                            resources: [
                                '*'
                            ]
                        })
                    ]
                })
            }
        });
    }
}