package Interfaces;

import Models.Entity;

public interface AttackManager extends ArenaListener
{
    Entity handleAttack();
}
