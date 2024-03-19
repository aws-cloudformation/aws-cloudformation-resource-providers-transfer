# AWS::Transfer::Server ProtocolDetails

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#passiveip" title="PassiveIp">PassiveIp</a>" : <i>String</i>,
    "<a href="#tlssessionresumptionmode" title="TlsSessionResumptionMode">TlsSessionResumptionMode</a>" : <i>String</i>,
    "<a href="#setstatoption" title="SetStatOption">SetStatOption</a>" : <i>String</i>,
    "<a href="#as2transports" title="As2Transports">As2Transports</a>" : <i>[ String, ... ]</i>
}
</pre>

### YAML

<pre>
<a href="#passiveip" title="PassiveIp">PassiveIp</a>: <i>String</i>
<a href="#tlssessionresumptionmode" title="TlsSessionResumptionMode">TlsSessionResumptionMode</a>: <i>String</i>
<a href="#setstatoption" title="SetStatOption">SetStatOption</a>: <i>String</i>
<a href="#as2transports" title="As2Transports">As2Transports</a>: <i>
      - String</i>
</pre>

## Properties

#### PassiveIp

_Required_: No

_Type_: String

_Maximum Length_: <code>15</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### TlsSessionResumptionMode

_Required_: No

_Type_: String

_Allowed Values_: <code>DISABLED</code> | <code>ENABLED</code> | <code>ENFORCED</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SetStatOption

_Required_: No

_Type_: String

_Allowed Values_: <code>DEFAULT</code> | <code>ENABLE_NO_OP</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### As2Transports

_Required_: No

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

