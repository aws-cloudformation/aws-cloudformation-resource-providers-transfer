# AWS::Transfer::User

Definition of AWS::Transfer::User Resource Type

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::Transfer::User",
    "Properties" : {
        "<a href="#homedirectory" title="HomeDirectory">HomeDirectory</a>" : <i>String</i>,
        "<a href="#homedirectorymappings" title="HomeDirectoryMappings">HomeDirectoryMappings</a>" : <i>[ <a href="homedirectorymapentry.md">HomeDirectoryMapEntry</a>, ... ]</i>,
        "<a href="#homedirectorytype" title="HomeDirectoryType">HomeDirectoryType</a>" : <i>String</i>,
        "<a href="#policy" title="Policy">Policy</a>" : <i>String</i>,
        "<a href="#posixprofile" title="PosixProfile">PosixProfile</a>" : <i><a href="posixprofile.md">PosixProfile</a></i>,
        "<a href="#role" title="Role">Role</a>" : <i>String</i>,
        "<a href="#serverid" title="ServerId">ServerId</a>" : <i>String</i>,
        "<a href="#sshpublickeys" title="SshPublicKeys">SshPublicKeys</a>" : <i>[ String, ... ]</i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tag.md">Tag</a>, ... ]</i>,
        "<a href="#username" title="UserName">UserName</a>" : <i>String</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::Transfer::User
Properties:
    <a href="#homedirectory" title="HomeDirectory">HomeDirectory</a>: <i>String</i>
    <a href="#homedirectorymappings" title="HomeDirectoryMappings">HomeDirectoryMappings</a>: <i>
      - <a href="homedirectorymapentry.md">HomeDirectoryMapEntry</a></i>
    <a href="#homedirectorytype" title="HomeDirectoryType">HomeDirectoryType</a>: <i>String</i>
    <a href="#policy" title="Policy">Policy</a>: <i>String</i>
    <a href="#posixprofile" title="PosixProfile">PosixProfile</a>: <i><a href="posixprofile.md">PosixProfile</a></i>
    <a href="#role" title="Role">Role</a>: <i>String</i>
    <a href="#serverid" title="ServerId">ServerId</a>: <i>String</i>
    <a href="#sshpublickeys" title="SshPublicKeys">SshPublicKeys</a>: <i>
      - String</i>
    <a href="#tags" title="Tags">Tags</a>: <i>
      - <a href="tag.md">Tag</a></i>
    <a href="#username" title="UserName">UserName</a>: <i>String</i>
</pre>

## Properties

#### HomeDirectory

_Required_: No

_Type_: String

_Maximum Length_: <code>1024</code>

_Pattern_: <code>^(|/.*)$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### HomeDirectoryMappings

_Required_: No

_Type_: List of <a href="homedirectorymapentry.md">HomeDirectoryMapEntry</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### HomeDirectoryType

_Required_: No

_Type_: String

_Allowed Values_: <code>PATH</code> | <code>LOGICAL</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Policy

_Required_: No

_Type_: String

_Maximum Length_: <code>2048</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### PosixProfile

_Required_: No

_Type_: <a href="posixprofile.md">PosixProfile</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Role

_Required_: Yes

_Type_: String

_Minimum Length_: <code>20</code>

_Maximum Length_: <code>2048</code>

_Pattern_: <code>^arn:.*role/\S+$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ServerId

_Required_: Yes

_Type_: String

_Minimum Length_: <code>19</code>

_Maximum Length_: <code>19</code>

_Pattern_: <code>^s-([0-9a-f]{17})$</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### SshPublicKeys

This represents the SSH User Public Keys for CloudFormation resource

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Tags

_Required_: No

_Type_: List of <a href="tag.md">Tag</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### UserName

_Required_: Yes

_Type_: String

_Minimum Length_: <code>3</code>

_Maximum Length_: <code>100</code>

_Pattern_: <code>^[\w][\w@.-]{2,99}$</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the Arn.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### Arn

Returns the <code>Arn</code> value.

