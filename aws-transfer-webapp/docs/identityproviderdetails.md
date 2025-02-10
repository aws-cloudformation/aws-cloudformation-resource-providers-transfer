# AWS::Transfer::WebApp IdentityProviderDetails

You can provide a structure that contains the details for the identity provider to use with your web app.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#instancearn" title="InstanceArn">InstanceArn</a>" : <i>String</i>,
    "<a href="#role" title="Role">Role</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#instancearn" title="InstanceArn">InstanceArn</a>: <i>String</i>
<a href="#role" title="Role">Role</a>: <i>String</i>
</pre>

## Properties

#### InstanceArn

The Amazon Resource Name (ARN) for the IAM Identity Center used for the web app.

_Required_: No

_Type_: String

_Minimum Length_: <code>10</code>

_Maximum Length_: <code>1224</code>

_Pattern_: <code>^arn:[\w-]+:sso:::instance/(sso)?ins-[a-zA-Z0-9-.]{16}$</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### Role

The IAM role in IAM Identity Center used for the web app.

_Required_: No

_Type_: String

_Minimum Length_: <code>20</code>

_Maximum Length_: <code>2048</code>

_Pattern_: <code>^arn:[a-z-]+:iam::[0-9]{12}:role[:/]\S+$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

