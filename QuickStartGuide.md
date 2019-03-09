# Corda Token SDK Quick Start Guide

The Corda Token SDK aims to provide a comprehensive reference implementation of common behaviours and data related to tokens. Those wishing to create a new token should use this guide as a starting point for token implementation.

For specific scenarios, such as representing fiat currencies or asset tokenisation, please see other planned entries in the Quick Start Guide series.


## What is a Corda Token?
A Corda token, as used by the Corda Token SDK, is a representation of tradeable value with an issuer and an owner. The token represents a liability (or obligation) of the issuer for the current owner. The current owner may transfer, or move, the token to a new owner. Please see README for more details on what is considered a token.

Additionally, these tokens can be considered discrete (or unique or non-fungible) or divisible and mergeable (or countable or fungible). A discrete token is exchanged in full and cannot be divided or merged, whereas a divisible/mergeable token may be exchanged in part.

In Ethereum, this roughly aligns to the ERC-721 (non-fungible token) and ERC-20 (fungible token) specifications (see the discussion on fungibility, below).


## Anatomy of a Corda Token

The core assumptions for a Corda Token SDK token implementation are as follows:

The Token must...
* be issued by some entity (R01)
* be owned by some entity (R02)
* support being issued (R03), moved (R04), and redeemed (R05) (opt exited)

The Token may...
* define the token in one of two separate means:
  1. Be embedded within the exchanged states (R06)
  1. Be linked via reference to a separate state (R07)
* exhibit one of two separate behaviours during a transaction:
  1. Be fungible (i.e. divisible and mergeable) (R08)
  1. Be discrete (i.e. unique and distinct) (R09)

As with all Cordapps, a Corda Token is implemented using interrelated components. These components severally define behaviour and jointly define the Token; that is, a Token is the sum of all components.

A Token is composed of the following:
1. **Definitions** (`Token`; opt `TokenType`, `TokenDefinition`) that describes the token's attributes (what is it) as well as the token's issuer (who issued it).
1. Current **Ownership** via *States* (`TokenState`) that defines who currently owns this token.
1. **Commands** (`TokenCommand` and implementations) that define the actions permissable with this token.
1. Token **Verification** via *Contracts* (`TokenContract`) that verifies states and state transitions.
1. **Workflows** via *Flows* (`*TokenWorkflow`) that enable transactions using these tokens.

This is separation of concern is distinct from other platforms such as Ethereum whereby the Token's definition, state, verification logic, and actions are all embedded within a single code-point.

With the Corda Token design, it is possible to rapidly issue, move, and redeem a new Token by implementing minimal code, instead relying upon standard Token SDK features for many simple Tokens. Tokens requiring more advanced design can, of course, build upon the Token SDK.


### Token Definitions

The **Token Definition** is arguably the most important part of the Token as it defines what is being represented and transacted on the Corda ledger. Common examples would be to represent a currency or commodity depository receipt, held off ledger, or a cryptocurrency, held on ledger. Other examples may be to represent a discrete item such as real estate or precious gems.

This definition may be represented in one of two ways - either as an embedded definition within the token's states (R06), or as a pointer to another state on the ledger (R07).





# Appendix

## A note on fungibility

Fungibility is defined by [Merriam-Webster](https://www.merriam-webster.com/dictionary/fungible) as

> being something (such as money or a commodity) of such a nature that one part or quantity may be replaced by another equal part or quantity in paying a debt or settling an account

[Wikipedia](https://en.wikipedia.org/wiki/Fungibility) describes fungibility within finance as

> [...] one unit of the good is substantially equivalent to another unit of the same good of the same quality at the same time and place.

Wikipedia further uses fiat currencies and cryptocurrencies as examples of fungible goods, and diamonds as an example of a (largely) non-fungible good.

[Cambridge Dictionary](https://dictionary.cambridge.org/dictionary/english/fungible) defines fungibiilty as

> easy to exchange or trade for something else of the same type and value

These definitions largely define *replace-ability* of the good rather than *divisibility and merge-ability* of the good as is understood in currency and cryptocurrency terms, although Merriam-Webster does offer some consideration for part replacement.

A discrete item, such as a diamond, could be considered fungible if the exchanging parties agreed that that two nearly identical diamonds, as assessed by, say, GIA, carried the same grading. In such a case, the two diamonds could be considered fungible.

# Footnotes

[^1]: Please see *A note on fungibility* for further discussion on fungible and non-fungible tokens.

[^2]: Alternative names: 'exited'