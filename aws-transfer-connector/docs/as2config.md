# AWS::Transfer::Connector As2Config

Configuration for an AS2 connector.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#localprofileid" title="LocalProfileId">LocalProfileId</a>" : <i>String</i>,
    "<a href="#partnerprofileid" title="PartnerProfileId">PartnerProfileId</a>" : <i>String</i>,
    "<a href="#messagesubject" title="MessageSubject">MessageSubject</a>" : <i>String</i>,
    "<a href="#compression" title="Compression">Compression</a>" : <i>String</i>,
    "<a href="#encryptionalgorithm" title="EncryptionAlgorithm">EncryptionAlgorithm</a>" : <i>String</i>,
    "<a href="#signingalgorithm" title="SigningAlgorithm">SigningAlgorithm</a>" : <i>String</i>,
    "<a href="#mdnsigningalgorithm" title="MdnSigningAlgorithm">MdnSigningAlgorithm</a>" : <i>String</i>,
    "<a href="#mdnresponse" title="MdnResponse">MdnResponse</a>" : <i>String</i>,
    "<a href="#basicauthsecretid" title="BasicAuthSecretId">BasicAuthSecretId</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#localprofileid" title="LocalProfileId">LocalProfileId</a>: <i>String</i>
<a href="#partnerprofileid" title="PartnerProfileId">PartnerProfileId</a>: <i>String</i>
<a href="#messagesubject" title="MessageSubject">MessageSubject</a>: <i>String</i>
<a href="#compression" title="Compression">Compression</a>: <i>String</i>
<a href="#encryptionalgorithm" title="EncryptionAlgorithm">EncryptionAlgorithm</a>: <i>String</i>
<a href="#signingalgorithm" title="SigningAlgorithm">SigningAlgorithm</a>: <i>String</i>
<a href="#mdnsigningalgorithm" title="MdnSigningAlgorithm">MdnSigningAlgorithm</a>: <i>String</i>
<a href="#mdnresponse" title="MdnResponse">MdnResponse</a>: <i>String</i>
<a href="#basicauthsecretid" title="BasicAuthSecretId">BasicAuthSecretId</a>: <i>String</i>
</pre>

## Properties

#### LocalProfileId

A unique identifier for the local profile.

_Required_: No

_Type_: String

_Minimum Length_: <code>19</code>

_Maximum Length_: <code>19</code>

_Pattern_: <code>^p-([0-9a-f]{17})$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### PartnerProfileId

A unique identifier for the partner profile.

_Required_: No

_Type_: String

_Minimum Length_: <code>19</code>

_Maximum Length_: <code>19</code>

_Pattern_: <code>^p-([0-9a-f]{17})$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MessageSubject

The message subject for this AS2 connector configuration.

_Required_: No

_Type_: String

_Minimum Length_: <code>1</code>

_Maximum Length_: <code>1024</code>

_Pattern_: <code>^[\u0020-\u007E\t]+$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Compression

Compression setting for this AS2 connector configuration.

_Required_: No

_Type_: String

_Allowed Values_: <code>ZLIB</code> | <code>DISABLED</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### EncryptionAlgorithm

Encryption algorithm for this AS2 connector configuration.

_Required_: No

_Type_: String

_Allowed Values_: <code>AES128_CBC</code> | <code>AES192_CBC</code> | <code>AES256_CBC</code> | <code>NONE</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SigningAlgorithm

Signing algorithm for this AS2 connector configuration.

_Required_: No

_Type_: String

_Allowed Values_: <code>SHA256</code> | <code>SHA384</code> | <code>SHA512</code> | <code>SHA1</code> | <code>NONE</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MdnSigningAlgorithm

MDN Signing algorithm for this AS2 connector configuration.

_Required_: No

_Type_: String

_Allowed Values_: <code>SHA256</code> | <code>SHA384</code> | <code>SHA512</code> | <code>SHA1</code> | <code>NONE</code> | <code>DEFAULT</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MdnResponse

MDN Response setting for this AS2 connector configuration.

_Required_: No

_Type_: String

_Allowed Values_: <code>SYNC</code> | <code>NONE</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### BasicAuthSecretId

ARN or name of the secret in AWS Secrets Manager which contains the credentials for Basic authentication. If empty, Basic authentication is disabled for the AS2 connector

_Required_: No

_Type_: String

_Maximum Length_: <code>2048</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

