# AWS::Transfer::Profile

Resource Type definition for AWS::Transfer::Profile

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::Transfer::Profile",
    "Properties" : {
        "<a href="#as2id" title="As2Id">As2Id</a>" : <i>String</i>,
        "<a href="#profiletype" title="ProfileType">ProfileType</a>" : <i>String</i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tag.md">Tag</a>, ... ]</i>,
        "<a href="#certificateids" title="CertificateIds">CertificateIds</a>" : <i>[ String, ... ]</i>,
    }
}
</pre>

### YAML

<pre>
Type: AWS::Transfer::Profile
Properties:
    <a href="#as2id" title="As2Id">As2Id</a>: <i>String</i>
    <a href="#profiletype" title="ProfileType">ProfileType</a>: <i>String</i>
    <a href="#tags" title="Tags">Tags</a>: <i>
      - <a href="tag.md">Tag</a></i>
    <a href="#certificateids" title="CertificateIds">CertificateIds</a>: <i>
      - String</i>
</pre>

## Properties

#### As2Id

AS2 identifier agreed with a trading partner.

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>128</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ProfileType

Enum specifying whether the profile is local or associated with a trading partner.

_Required_: Yes

_Type_: String

_Allowed Values_: <code>LOCAL</code> | <code>PARTNER</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### Tags

An array of key-value pairs to apply to this resource.

_Required_: No

_Type_: List of <a href="tag.md">Tag</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### CertificateIds

List of the certificate IDs associated with this profile to be used for encryption and signing of AS2 messages.

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the ProfileId.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### Arn

Specifies the unique Amazon Resource Name (ARN) for the profile.

#### ProfileId

A unique identifier for the profile

