# AWS::Transfer::Connector

Resource Type definition for AWS::Transfer::Connector

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::Transfer::Connector",
    "Properties" : {
        "<a href="#accessrole" title="AccessRole">AccessRole</a>" : <i>String</i>,
        "<a href="#as2config" title="As2Config">As2Config</a>" : <i><a href="as2config.md">As2Config</a></i>,
        "<a href="#sftpconfig" title="SftpConfig">SftpConfig</a>" : <i><a href="sftpconfig.md">SftpConfig</a></i>,
        "<a href="#loggingrole" title="LoggingRole">LoggingRole</a>" : <i>String</i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tag.md">Tag</a>, ... ]</i>,
        "<a href="#url" title="Url">Url</a>" : <i>String</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::Transfer::Connector
Properties:
    <a href="#accessrole" title="AccessRole">AccessRole</a>: <i>String</i>
    <a href="#as2config" title="As2Config">As2Config</a>: <i><a href="as2config.md">As2Config</a></i>
    <a href="#sftpconfig" title="SftpConfig">SftpConfig</a>: <i><a href="sftpconfig.md">SftpConfig</a></i>
    <a href="#loggingrole" title="LoggingRole">LoggingRole</a>: <i>String</i>
    <a href="#tags" title="Tags">Tags</a>: <i>
      - <a href="tag.md">Tag</a></i>
    <a href="#url" title="Url">Url</a>: <i>String</i>
</pre>

## Properties

#### AccessRole

Specifies the access role for the connector.

_Required_: Yes

_Type_: String

_Minimum Length_: <code>20</code>

_Maximum Length_: <code>2048</code>

_Pattern_: <code>arn:.*role/.*</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### As2Config

Configuration for an AS2 connector.

_Required_: No

_Type_: <a href="as2config.md">As2Config</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SftpConfig

Configuration for an SFTP connector.

_Required_: No

_Type_: <a href="sftpconfig.md">SftpConfig</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### LoggingRole

Specifies the logging role for the connector.

_Required_: No

_Type_: String

_Minimum Length_: <code>20</code>

_Maximum Length_: <code>2048</code>

_Pattern_: <code>arn:.*role/.*</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Tags

Key-value pairs that can be used to group and search for connectors. Tags are metadata attached to connectors for any purpose.

_Required_: No

_Type_: List of <a href="tag.md">Tag</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Url

URL for Connector

_Required_: Yes

_Type_: String

_Maximum Length_: <code>255</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the ConnectorId.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### Arn

Specifies the unique Amazon Resource Name (ARN) for the connector.

#### ConnectorId

A unique identifier for the connector.

#### ServiceManagedEgressIpAddresses

The list of egress IP addresses of this connector. These IP addresses are assigned automatically when you create the connector.

