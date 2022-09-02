# AWS::Transfer::Agreement

Resource Type definition for AWS::Transfer::Agreement

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::Transfer::Agreement",
    "Properties" : {
        "<a href="#description" title="Description">Description</a>" : <i>String</i>,
        "<a href="#serverid" title="ServerId">ServerId</a>" : <i>String</i>,
        "<a href="#localprofileid" title="LocalProfileId">LocalProfileId</a>" : <i>String</i>,
        "<a href="#partnerprofileid" title="PartnerProfileId">PartnerProfileId</a>" : <i>String</i>,
        "<a href="#basedirectory" title="BaseDirectory">BaseDirectory</a>" : <i>String</i>,
        "<a href="#accessrole" title="AccessRole">AccessRole</a>" : <i>String</i>,
        "<a href="#status" title="Status">Status</a>" : <i>String</i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tag.md">Tag</a>, ... ]</i>,
    }
}
</pre>

### YAML

<pre>
Type: AWS::Transfer::Agreement
Properties:
    <a href="#description" title="Description">Description</a>: <i>String</i>
    <a href="#serverid" title="ServerId">ServerId</a>: <i>String</i>
    <a href="#localprofileid" title="LocalProfileId">LocalProfileId</a>: <i>String</i>
    <a href="#partnerprofileid" title="PartnerProfileId">PartnerProfileId</a>: <i>String</i>
    <a href="#basedirectory" title="BaseDirectory">BaseDirectory</a>: <i>String</i>
    <a href="#accessrole" title="AccessRole">AccessRole</a>: <i>String</i>
    <a href="#status" title="Status">Status</a>: <i>String</i>
    <a href="#tags" title="Tags">Tags</a>: <i>
      - <a href="tag.md">Tag</a></i>
</pre>

## Properties

#### Description

A textual description for the agreement.

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>200</code>

_Pattern_: <code>^[\w\- ]*$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ServerId

A unique identifier for the server.

_Required_: Yes

_Type_: String

_Minimum_: <code>19</code>

_Maximum_: <code>19</code>

_Pattern_: <code>^s-([0-9a-f]{17})$</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### LocalProfileId

A unique identifier for the local profile.

_Required_: Yes

_Type_: String

_Minimum_: <code>19</code>

_Maximum_: <code>19</code>

_Pattern_: <code>^p-([0-9a-f]{17})$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### PartnerProfileId

A unique identifier for the partner profile.

_Required_: Yes

_Type_: String

_Minimum_: <code>19</code>

_Maximum_: <code>19</code>

_Pattern_: <code>^p-([0-9a-f]{17})$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### BaseDirectory

Specifies the base directory for the agreement.

_Required_: Yes

_Type_: String

_Maximum_: <code>1024</code>

_Pattern_: <code>^$|/.*</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### AccessRole

Specifies the access role for the agreement.

_Required_: Yes

_Type_: String

_Minimum_: <code>20</code>

_Maximum_: <code>2048</code>

_Pattern_: <code>arn:.*role/.*</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Status

Specifies the status of the agreement.

_Required_: No

_Type_: String

_Allowed Values_: <code>ACTIVE</code> | <code>INACTIVE</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Tags

Key-value pairs that can be used to group and search for agreements. Tags are metadata attached to agreements for any purpose.

_Required_: No

_Type_: List of <a href="tag.md">Tag</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### AgreementId

A unique identifier for the agreement.

#### Arn

Specifies the unique Amazon Resource Name (ARN) for the agreement.

