# AWS::Transfer::Certificate

Resource Type definition for AWS::Transfer::Certificate

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::Transfer::Certificate",
    "Properties" : {
        "<a href="#usage" title="Usage">Usage</a>" : <i>String</i>,
        "<a href="#certificate" title="Certificate">Certificate</a>" : <i>String</i>,
        "<a href="#certificatechain" title="CertificateChain">CertificateChain</a>" : <i>String</i>,
        "<a href="#privatekey" title="PrivateKey">PrivateKey</a>" : <i>String</i>,
        "<a href="#activedate" title="ActiveDate">ActiveDate</a>" : <i>String</i>,
        "<a href="#inactivedate" title="InactiveDate">InactiveDate</a>" : <i>String</i>,
        "<a href="#description" title="Description">Description</a>" : <i>String</i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tag.md">Tag</a>, ... ]</i>,
    }
}
</pre>

### YAML

<pre>
Type: AWS::Transfer::Certificate
Properties:
    <a href="#usage" title="Usage">Usage</a>: <i>String</i>
    <a href="#certificate" title="Certificate">Certificate</a>: <i>String</i>
    <a href="#certificatechain" title="CertificateChain">CertificateChain</a>: <i>String</i>
    <a href="#privatekey" title="PrivateKey">PrivateKey</a>: <i>String</i>
    <a href="#activedate" title="ActiveDate">ActiveDate</a>: <i>String</i>
    <a href="#inactivedate" title="InactiveDate">InactiveDate</a>: <i>String</i>
    <a href="#description" title="Description">Description</a>: <i>String</i>
    <a href="#tags" title="Tags">Tags</a>: <i>
      - <a href="tag.md">Tag</a></i>
</pre>

## Properties

#### Usage

Specifies the usage type for the certificate.

_Required_: Yes

_Type_: String

_Allowed Values_: <code>SIGNING</code> | <code>ENCRYPTION</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Certificate

Specifies the certificate body to be imported.

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>16384</code>

_Pattern_: <code>^[

	 -ÿ]*</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### CertificateChain

Specifies the certificate chain to be imported.

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>2097152</code>

_Pattern_: <code>^[

	 -ÿ]*</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### PrivateKey

Specifies the private key for the certificate.

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>16384</code>

_Pattern_: <code>^[

	 -ÿ]*</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### ActiveDate

Specifies the active date for the certificate.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### InactiveDate

Specifies the inactive date for the certificate.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Description

A textual description for the certificate.

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>200</code>

_Pattern_: <code>^[\w\- ]*$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Tags

Key-value pairs that can be used to group and search for certificates. Tags are metadata attached to certificates for any purpose.

_Required_: No

_Type_: List of <a href="tag.md">Tag</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the CertificateId.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### Arn

Specifies the unique Amazon Resource Name (ARN) for the agreement.

#### CertificateId

A unique identifier for the certificate.

#### Status

A status description for the certificate.

#### Type

Describing the type of certificate. With or without a private key.

#### Serial

Specifies Certificate's serial.

#### NotAfterDate

Specifies the not after date for the certificate.

#### NotBeforeDate

Specifies the not before date for the certificate.

