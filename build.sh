#!/usr/bin/env zsh

gradle deployNodes

cp defaults/node_notary.conf build/nodes/Notary/node.conf
cp defaults/node_oracle.conf build/nodes/Oracle/node.conf
cp defaults/node_subscriber.conf build/nodes/Subscriber/node.conf
cp defaults/node_turkcell.conf build/nodes/TurkCell/node.conf
