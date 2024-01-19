# AWS::Transfer::Server

Resource Type definition for AWS::Transfer::Server

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::Transfer::Server",
    "Properties" : {
        "<a href="#certificate" title="Certificate">Certificate</a>" : <i>String</i>,
        "<a href="#domain" title="Domain">Domain</a>" : <i>String</i>,
        "<a href="#endpointdetails" title="EndpointDetails">EndpointDetails</a>" : <i><a href="endpointdetails.md">EndpointDetails</a></i>,
        "<a href="#endpointtype" title="EndpointType">EndpointType</a>" : <i>String</i>,
        "<a href="#identityproviderdetails" title="IdentityProviderDetails">IdentityProviderDetails</a>" : <i><a href="identityproviderdetails.md">IdentityProviderDetails</a></i>,
        "<a href="#identityprovidertype" title="IdentityProviderType">IdentityProviderType</a>" : <i>String</i>,
        "<a href="#loggingrole" title="LoggingRole">LoggingRole</a>" : <i>String</i>,
        "<a href="#postauthenticationloginbanner" title="PostAuthenticationLoginBanner">PostAuthenticationLoginBanner</a>" : <i>String</i>,
        "<a href="#preauthenticationloginbanner" title="PreAuthenticationLoginBanner">PreAuthenticationLoginBanner</a>" : <i>String</i>,
        "<a href="#protocoldetails" title="ProtocolDetails">ProtocolDetails</a>" : <i><a href="protocoldetails.md">ProtocolDetails</a></i>,
        "<a href="#protocols" title="Protocols">Protocols</a>" : <i>[ String, ... ]</i>,
        "<a href="#securitypolicyname" title="SecurityPolicyName">SecurityPolicyName</a>" : <i>String</i>,
        "<a href="#structuredlogdestinations" title="StructuredLogDestinations">StructuredLogDestinations</a>" : <i>[ String, ... ]</i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tag.md">Tag</a>, ... ]</i>,
        "<a href="#workflowdetails" title="WorkflowDetails">WorkflowDetails</a>" : <i><a href="workflowdetails.md">WorkflowDetails</a></i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::Transfer::Server
Properties:
    <a href="#certificate" title="Certificate">Certificate</a>: <i>String</i>
    <a href="#domain" title="Domain">Domain</a>: <i>String</i>
    <a href="#endpointdetails" title="EndpointDetails">EndpointDetails</a>: <i><a href="endpointdetails.md">EndpointDetails</a></i>
    <a href="#endpointtype" title="EndpointType">EndpointType</a>: <i>String</i>
    <a href="#identityproviderdetails" title="IdentityProviderDetails">IdentityProviderDetails</a>: <i><a href="identityproviderdetails.md">IdentityProviderDetails</a></i>
    <a href="#identityprovidertype" title="IdentityProviderType">IdentityProviderType</a>: <i>String</i>
    <a href="#loggingrole" title="LoggingRole">LoggingRole</a>: <i>String</i>
    <a href="#postauthenticationloginbanner" title="PostAuthenticationLoginBanner">PostAuthenticationLoginBanner</a>: <i>String</i>
    <a href="#preauthenticationloginbanner" title="PreAuthenticationLoginBanner">PreAuthenticationLoginBanner</a>: <i>String</i>
    <a href="#protocoldetails" title="ProtocolDetails">ProtocolDetails</a>: <i><a href="protocoldetails.md">ProtocolDetails</a></i>
    <a href="#protocols" title="Protocols">Protocols</a>: <i>
      - String</i>
    <a href="#securitypolicyname" title="SecurityPolicyName">SecurityPolicyName</a>: <i>String</i>
    <a href="#structuredlogdestinations" title="StructuredLogDestinations">StructuredLogDestinations</a>: <i>
      - String</i>
    <a href="#tags" title="Tags">Tags</a>: <i>
      - <a href="tag.md">Tag</a></i>
    <a href="#workflowdetails" title="WorkflowDetails">WorkflowDetails</a>: <i><a href="workflowdetails.md">WorkflowDetails</a></i>
</pre>

## Properties

#### Certificate

_Required_: No

_Type_: String

_Maximum Length_: <code>1600</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Domain

_Required_: No

_Type_: String

_Allowed Values_: <code>S3</code> | <code>EFS</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### EndpointDetails

_Required_: No

_Type_: <a href="endpointdetails.md">EndpointDetails</a>

_Update requires_: [Some interruptions](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-some-interrupt)

#### EndpointType

_Required_: No

_Type_: String

_Allowed Values_: <code>PUBLIC</code> | <code>VPC</code> | <code>VPC_ENDPOINT</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### IdentityProviderDetails

_Required_: No

_Type_: <a href="identityproviderdetails.md">IdentityProviderDetails</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### IdentityProviderType

_Required_: No

_Type_: String

_Allowed Values_: <code>SERVICE_MANAGED</code> | <code>API_GATEWAY</code> | <code>AWS_DIRECTORY_SERVICE</code> | <code>AWS_LAMBDA</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### LoggingRole

_Required_: No

_Type_: String

_Maximum Length_: <code>2048</code>

_Pattern_: <code>^$|arn:.*role/</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### PostAuthenticationLoginBanner

_Required_: No

_Type_: String

_Maximum Length_: <code>512</code>

_Pattern_: <code>^[\x09-\x0D\x20-\x7E]*$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### PreAuthenticationLoginBanner

_Required_: No

_Type_: String

_Maximum Length_: <code>512</code>

_Pattern_: <code>^[\x09-\x0D\x20-\x7E]*$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ProtocolDetails

_Required_: No

_Type_: <a href="protocoldetails.md">ProtocolDetails</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Protocols

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SecurityPolicyName

_Required_: No

_Type_: String

_Maximum Length_: <code>100</code>

_Pattern_: <code>^TransferSecurityPolicy-.+$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### StructuredLogDestinations

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Tags

_Required_: No

_Type_: List of <a href="tag.md">Tag</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### WorkflowDetails

_Required_: No

_Type_: <a href="workflowdetails.md">WorkflowDetails</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the Arn.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### Arn

Returns the <code>Arn</code> value.

#### ServerId

Returns the <code>ServerId</code> value.

#### As2ServiceManagedEgressIpAddresses

The list of egress IP addresses of this server. These IP addresses are only relevant for servers that use the AS2 protocol. They are used for sending asynchronous MDNs. These IP addresses are assigned automatically when you create an AS2 server. Additionally, if you update an existing server and add the AS2 protocol, static IP addresses are assigned as well.

